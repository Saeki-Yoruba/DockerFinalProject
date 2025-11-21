package com.supernovapos.finalproject.auth.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.supernovapos.finalproject.auth.model.dto.AuthRequest;
import com.supernovapos.finalproject.auth.model.dto.AuthResponse;
import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.auth.model.mapper.AuthMapper;
import com.supernovapos.finalproject.auth.security.CustomUserDetails;
import com.supernovapos.finalproject.auth.security.CustomUserDetailsService;
import com.supernovapos.finalproject.auth.security.JwtUtil;
import com.supernovapos.finalproject.auth.service.AuthService;
import com.supernovapos.finalproject.common.exception.AuthException;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.oauth.verifier.GoogleTokenVerifier;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.repository.UserRepository;
import com.supernovapos.finalproject.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final AuthMapper authMapper;

    /**
     * 取得目前登入的 Authentication
     */
    @Transactional(readOnly = true)
    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null 
                || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("嘗試存取目前登入資訊，但使用者未登入或為匿名狀態");
            throw new IllegalStateException("尚未登入，無法取得使用者資訊");
        }
        return authentication;
    }

    /**
     * 取得目前登入的 User ID
     */
    @Override
    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        Object principal = getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            Long userId = userDetails.getUser().getId();
            log.debug("取得目前登入的 UserId = {}", userId);
            return userId;
        }
        throw new IllegalStateException("無法解析登入使用者資訊");
    }

    /**
     * 取得目前登入的完整 User 實體
     */
    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Object principal = getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            log.debug("取得目前登入的 User 實體: {}", userDetails.getUsername());
            return userDetails.getUser();
        }
        throw new IllegalStateException("無法解析登入使用者資訊");
    }

    /**
     * 帳號密碼登入流程
     */
    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            log.info("使用者嘗試登入，帳號 = {}", request.getUsername());
        } catch (BadCredentialsException ex) {
            log.warn("登入失敗：帳號或密碼錯誤，帳號 = {}", request.getUsername());
            throw new AuthException("帳號或密碼錯誤", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED.value());
        }

        User user = userService.findByEmailOrPhoneOrThrow(request.getUsername());
        validateUserStatus(user);

        log.info("使用者登入成功，userId = {}, email = {}", user.getId(), user.getEmail());
        return buildAuthResponse(user);
    }

    /**
     * Google 快速登入
     */
    @Override
    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);
        String googleUid = payload.getSubject();

        log.info("Google 登入請求，googleUid = {}", googleUid);

        User user = userRepository.findByGoogleUid(googleUid)
                .orElseThrow(() -> {
                    log.warn("Google 登入失敗：帳號未綁定，googleUid = {}", googleUid);
                    return new InvalidRequestException("此 Google 帳號尚未綁定，請先用一般帳號登入並綁定");
                });

        validateUserStatus(user);

        log.info("Google 登入成功，userId = {}, email = {}", user.getId(), user.getEmail());
        return buildAuthResponse(user);
    }

    /**
     * LINE 快速登入
     */
    @Override
    @Transactional
    public AuthResponse loginWithLine(String accessToken) {
        try {
            String profile = restClient.get()
                    .uri("https://api.line.me/v2/profile")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            JsonNode profileJson = objectMapper.readTree(profile);
            String lineUid = profileJson.get("userId").asText();

            log.info("LINE 登入請求，lineUid = {}", lineUid);

            User user = userRepository.findByLineUid(lineUid)
                    .orElseThrow(() -> {
                        log.warn("LINE 登入失敗：帳號未綁定，lineUid = {}", lineUid);
                        return new InvalidRequestException("此 LINE 帳號尚未綁定，請先用一般帳號登入並綁定");
                    });

            validateUserStatus(user);

            log.info("LINE 登入成功，userId = {}, email = {}", user.getId(), user.getEmail());
            return buildAuthResponse(user);

        } catch (Exception e) {
            log.error("LINE 登入過程發生錯誤", e);
            throw new AuthException("LINE 登入失敗", "LINE_LOGIN_FAILED", HttpStatus.UNAUTHORIZED.value());
        }
    }

    /**
     * 共用：檢查帳號狀態
     */
    private void validateUserStatus(User user) {
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            log.warn("登入驗證失敗：帳號尚未驗證，userId = {}", user.getId());
            throw new AuthException("帳號尚未驗證", "UNVERIFIED_ACCOUNT", HttpStatus.FORBIDDEN.value());
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("登入驗證失敗：帳號已停用，userId = {}", user.getId());
            throw new AuthException("帳號已停用", "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN.value());
        }
    }

    /**
     * 共用：產生 AuthResponse
     */
    @Transactional(readOnly = true)
    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwt = jwtUtil.generateToken(userDetails);

        AuthResponse response = authMapper.toAuthResponse(user);
        response.setToken(jwt);

        List<Role> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .toList();

        response.setRoles(roles.stream().map(Role::getCode).toList());
        response.setRoleCategories(
                roles.stream().map(r -> r.getCategory().name()).distinct().toList()
        );

        return response;
    }
}
