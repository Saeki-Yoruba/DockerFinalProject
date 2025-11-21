package com.supernovapos.finalproject.user.service.impl;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.supernovapos.finalproject.auth.service.AuthService;
import com.supernovapos.finalproject.common.exception.ConflictException;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.oauth.verifier.GoogleTokenVerifier;
import com.supernovapos.finalproject.user.model.dto.BindResponseDto;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.model.mapper.BindResponseMapper;
import com.supernovapos.finalproject.user.repository.UserRepository;
import com.supernovapos.finalproject.user.service.UserBindingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBindingServiceImpl implements UserBindingService {

    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final BindResponseMapper bindResponseMapper;

    /**
     * 綁定 Google 帳號
     * - 驗證 Google ID Token
     * - 檢查是否已被其他使用者綁定
     * - 更新 nickname / avatar（若原本沒有）
     * - 儲存並回傳結果
     */
    @Override
    @Transactional
    public BindResponseDto bindGoogle(String idToken) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);
        String googleUid = payload.getSubject();

        User currentUser = authService.getCurrentUser();
        updateUserSocialBinding(
                currentUser,
                googleUid,
                (String) payload.get("name"),
                (String) payload.get("picture"),
                userRepository::existsByGoogleUid,
                User::setGoogleUid,
                "Google"
        );

        return bindResponseMapper.toBindResponse(
                "Google",
                "帳號綁定成功",
                googleUid,
                payload.getEmail(),
                currentUser.getNickname()
        );
    }

    /**
     * 解除 Google 綁定
     * - 移除 googleUid
     * - 回傳解除結果
     */
    @Override
    @Transactional
    public BindResponseDto unbindGoogle() {
        User currentUser = authService.getCurrentUser();
        currentUser.setGoogleUid(null);
        userRepository.save(currentUser);

        return bindResponseMapper.toUnbindResponse("Google", currentUser.getEmail());
    }

    /**
     * 綁定 LINE 帳號
     * - 呼叫 LINE API 取得 profile
     * - 驗證 userId 是否有效
     * - 檢查是否已被其他使用者綁定
     * - 更新 nickname / avatar（若原本沒有）
     * - 儲存並回傳結果
     */
    @Override
    @Transactional
    public BindResponseDto bindLine(String accessToken) {
        try {
            JsonNode profile = fetchLineProfile(accessToken);

            String lineUid = profile.get("userId").asText();
            String displayName = profile.get("displayName").asText();
            String pictureUrl = profile.get("pictureUrl").asText();

            if (lineUid == null || lineUid.isBlank()) {
                throw new InvalidRequestException("[LINE] UID 無效");
            }

            User currentUser = authService.getCurrentUser();
            updateUserSocialBinding(
                    currentUser,
                    lineUid,
                    displayName,
                    pictureUrl,
                    userRepository::existsByLineUid,
                    User::setLineUid,
                    "LINE"
            );

            return bindResponseMapper.toBindResponse(
                    "LINE",
                    "帳號綁定成功",
                    lineUid,
                    null,
                    currentUser.getNickname()
            );
        } catch (ConflictException | InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[LINE] 綁定過程發生例外 userId={}", authService.getCurrentUser().getId(), e);
            throw new RuntimeException("[LINE] 綁定失敗", e);
        }
    }

    /**
     * 解除 LINE 綁定
     * - 移除 lineUid
     * - 回傳解除結果
     */
    @Override
    @Transactional
    public BindResponseDto unbindLine() {
        User currentUser = authService.getCurrentUser();
        currentUser.setLineUid(null);
        userRepository.save(currentUser);

        return bindResponseMapper.toUnbindResponse("LINE", currentUser.getEmail());
    }

    // ==================================================
    // 🔹 Private Helpers
    // ==================================================

    /**
     * 共用的社群帳號綁定邏輯
     * - 驗證是否已被其他使用者綁定
     * - 綁定 UID、補齊 nickname/avatar
     * - 儲存使用者
     */
    private void updateUserSocialBinding(
            User user,
            String uid,
            String nickname,
            String avatar,
            Function<String, Boolean> existsChecker,
            BiConsumer<User, String> uidSetter,
            String provider
    ) {
        if (existsChecker.apply(uid)) {
            throw new ConflictException("[" + provider + "] 此帳號已被其他使用者綁定");
        }

        uidSetter.accept(user, uid);

        if (user.getNickname() == null || user.getNickname().isBlank()) {
            user.setNickname(nickname);
        }
        if (user.getAvatar() == null) {
            user.setAvatar(avatar);
        }

        userRepository.save(user);
        log.info("使用者 {} 成功綁定 {} 帳號 uid={}", user.getId(), provider, uid);
    }

    /**
     * 呼叫 LINE API 取得使用者 Profile
     */
    private JsonNode fetchLineProfile(String accessToken) throws Exception {
        String result = restClient.get()
                .uri("https://api.line.me/v2/profile")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);
        return objectMapper.readTree(result);
    }
}

