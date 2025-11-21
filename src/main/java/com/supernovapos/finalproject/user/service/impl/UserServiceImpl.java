package com.supernovapos.finalproject.user.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.auth.model.entity.UserRoleId;
import com.supernovapos.finalproject.auth.model.entity.VerificationToken;
import com.supernovapos.finalproject.auth.repository.RoleRepository;
import com.supernovapos.finalproject.auth.repository.VerificationTokenRepository;
import com.supernovapos.finalproject.auth.service.EmailService;
import com.supernovapos.finalproject.common.exception.ConflictException;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.common.util.SystemAccountProtector;
import com.supernovapos.finalproject.order.model.Orders;
import com.supernovapos.finalproject.payment.service.PointService;
import com.supernovapos.finalproject.user.model.dto.UserOrderResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserRegisterDto;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserUpdateDto;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.model.mapper.OrderMapper;
import com.supernovapos.finalproject.user.model.mapper.UserMapper;
import com.supernovapos.finalproject.user.repository.UserOrderRepository;
import com.supernovapos.finalproject.user.repository.UserRepository;
import com.supernovapos.finalproject.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserOrderRepository userOrderRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemAccountProtector protector;
    private final PointService pointService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    // ==================================================
    // 🔹 使用者註冊 / 登入相關
    // ==================================================

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRegisterDto dto) {
        checkEmailUnique(dto.getEmail());
        checkPhoneUnique(dto.getPhone());

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setIsActive(false);

        User savedUser = userRepository.save(user);

        assignDefaultUserRole(savedUser);
        sendVerificationToken(savedUser);

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String username) {
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhoneNumber(username))
                .orElseThrow(() -> new ResourceNotFoundException("找不到使用者"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ConflictException("帳號已完成驗證");
        }

        sendVerificationToken(user);
    }

    @Override
    @Transactional
    public void verifyAccount(String token) {
        VerificationToken vToken = validateVerificationToken(token);

        User user = vToken.getUser();
        user.setEmailVerified(true);
        user.setIsActive(true);
        userRepository.save(user);

        tokenRepository.delete(vToken);
    }

    // ==================================================
    // 🔹 一般用戶操作
    // ==================================================

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到使用者"));

        protector.checkNotSystemAdmin(user.getId(), "修改資料");

        applyUserUpdates(user, dto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto updateUserStatus(Long id, boolean isAvailable) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到使用者"));

        protector.checkNotSystemAdmin(user.getId(), "停用超級管理員");
        validateNotAdmin(user, "停用管理員帳號");

        user.setIsActive(isAvailable);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到使用者"));

        validateNotAdmin(user, "停用管理員帳號");

        user.setIsActive(false);
        return userMapper.toDto(userRepository.save(user));
    }

    // ==================================================
    // 🔹 查詢
    // ==================================================

    @Override
    public UserResponseDto getCurrentUser(User user) {
        return userMapper.toDto(user);
    }

    @Override
    public Optional<UserResponseDto> findUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    public Page<UserOrderResponseDto> getMyOrders(User user,Pageable pageable) {
        Page<Orders> orders = userOrderRepository.findByUserId(user.getId(), pageable);
        return orders.map(orderMapper::toDto);
    }
    
    @Override
    public User findByEmailOrPhoneOrThrow(String input) {
        return userRepository.findByEmailOrPhoneWithRoles(input)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + input));
    }

    @Override
    public Page<UserResponseDto> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    // ==================================================
    // 🔹 Private Helpers
    // ==================================================

    /** 驗證 Email 是否唯一 */
    private void checkEmailUnique(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("此 Email 已被註冊");
        }
    }

    /** 驗證 Phone 是否唯一 */
    private void checkPhoneUnique(String phone) {
        if (userRepository.findByPhoneNumber(phone).isPresent()) {
            throw new ConflictException("此手機號碼已被註冊");
        }
    }

    /** 新增預設角色 ROLE_USER */
    private void assignDefaultUserRole(User user) {
        Role defaultRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("找不到預設角色 ROLE_USER"));
        user.getUserRoles().add(
                new UserRole(new UserRoleId(user.getId(), defaultRole.getId()), user, defaultRole)
        );
        userRepository.save(user);
    }

    /** 建立並寄送驗證信 */
    private void sendVerificationToken(User user) {
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        VerificationToken vToken = new VerificationToken();
        vToken.setUser(user);
        vToken.setToken(token);
        vToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(vToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    /** 驗證 token 是否有效 */
    private VerificationToken validateVerificationToken(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRequestException("驗證失敗，無效的 token"));

        if (vToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("驗證碼已過期，請重新索取驗證信");
        }
        return vToken;
    }

    /** 管理員保護檢查 */
    private void validateNotAdmin(User user, String action) {
        boolean isAdmin = user.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getCode()));
        if (isAdmin) {
            throw new InvalidRequestException(action + "：管理員帳號不能被操作");
        }
    }

    /** 套用使用者更新內容 */
    private void applyUserUpdates(User user, UserUpdateDto dto) {
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getInvoiceCarrier() != null) user.setInvoiceCarrier(dto.getInvoiceCarrier());
        
     // 首次設定生日贈送50點
        if (dto.getBirthdate() != null && user.getBirthdate() == null) {
            user.setBirthdate(dto.getBirthdate());
            pointService.grantPointsByAdmin(user.getId(), 50, "生日綁定贈送");
        } else if (dto.getBirthdate() != null) {
            // 已經有生日不再重送
            user.setBirthdate(dto.getBirthdate());
        }
    }
}

