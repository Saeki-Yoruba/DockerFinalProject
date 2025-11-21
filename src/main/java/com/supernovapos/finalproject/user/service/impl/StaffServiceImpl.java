package com.supernovapos.finalproject.user.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.auth.constant.RoleCategoryEnum;
import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.auth.model.entity.UserRoleId;
import com.supernovapos.finalproject.auth.repository.RoleRepository;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.user.model.dto.StaffRegisterDto;
import com.supernovapos.finalproject.user.model.dto.StaffRoleResponse;
import com.supernovapos.finalproject.user.model.dto.StaffUpdateDto;
import com.supernovapos.finalproject.user.model.dto.UserRegisterDto;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.model.dto.UserUpdateDto;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.model.mapper.UserMapper;
import com.supernovapos.finalproject.user.repository.UserRepository;
import com.supernovapos.finalproject.user.repository.UserRoleRepository;
import com.supernovapos.finalproject.user.service.StaffService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ==================================================
    // 🔹 Public Methods
    // ==================================================

    @Override
    @Transactional
    public UserResponseDto createStaff(StaffRegisterDto dto) {
        log.info("嘗試建立員工: email={}, phone={}", dto.getEmail(), dto.getPhoneNumber());

        User user = findOrCreateUser(dto);
        assignStoreRoles(user, dto.getRoles());

        User saved = userRepository.save(user);
        log.info("員工建立完成 id={}, roles={}", saved.getId(), dto.getRoles());
        return userMapper.toDto(saved);
    }

    @Override
    public Page<UserResponseDto> findAllStaff(Pageable pageable, String role) {
        Page<User> users = (role != null && !role.isBlank())
                ? userRepository.findAllByRoleCode(role, pageable)
                : userRepository.findAllByRoleCategory(RoleCategoryEnum.STORE, pageable);

        log.info("查詢員工: role={} total={}", role, users.getTotalElements());
        return users.map(userMapper::toDto);
    }

    @Override
    public List<StaffRoleResponse> getStaffRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工 id=" + userId));

        List<Role> activeStoreRoles = roleRepository.findByCategoryAndIsAvailableTrue(RoleCategoryEnum.STORE);
        Set<String> userRoleCodes = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getCode())
                .collect(Collectors.toSet());

        return activeStoreRoles.stream()
                .map(role -> new StaffRoleResponse(role.getCode(), role.getName(), userRoleCodes.contains(role.getCode())))
                .toList();
    }

    @Override
    @Transactional
    public UserResponseDto updateStaff(Long id, StaffUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工 id=" + id));

        updateBasicInfo(user, dto);

        if (dto.getRoles() != null) {
            updateStoreRoles(user, dto.getRoles());
        }

        User saved = userRepository.save(user);
        log.info("員工更新成功 id={}, roles={}", saved.getId(), dto.getRoles());
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserResponseDto deactivateStaff(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工 id=" + id));

        if (user.getUserRoles().stream().noneMatch(ur -> ur.getRole().getCategory() == RoleCategoryEnum.STORE)) {
            throw new InvalidRequestException("該使用者不是員工帳號");
        }

        user.getUserRoles().removeIf(ur -> ur.getRole().getCategory() == RoleCategoryEnum.STORE
                && !"ROLE_OWNER".equals(ur.getRole().getCode()));

        validateStoreRoleConstraints(user);

        User saved = userRepository.save(user);
        log.info("員工離職成功 id={}", saved.getId());
        return userMapper.toDto(saved);
    }

    // ==================================================
    // 🔹 Private Helpers
    // ==================================================

    /** 找現有帳號，或建立新帳號 */
    private User findOrCreateUser(StaffRegisterDto dto) {
        Optional<User> existingUserOpt = userRepository.findByEmail(dto.getEmail());
        if (existingUserOpt.isEmpty() && dto.getPhoneNumber() != null) {
            existingUserOpt = userRepository.findByPhoneNumber(dto.getPhoneNumber());
        }

        if (existingUserOpt.isPresent()) {
            log.warn("帳號已存在，沿用帳號 id={}", existingUserOpt.get().getId());
            return existingUserOpt.get();
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmailVerified(true);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    /** 更新員工基本資料 */
    private void updateBasicInfo(User user, StaffUpdateDto dto) {
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
    }

    /** 更新 STORE 類角色（保留 OWNER） */
    private void updateStoreRoles(User user, List<String> roleCodes) {
        user.getUserRoles().removeIf(ur -> ur.getRole().getCategory() == RoleCategoryEnum.STORE
                && !"ROLE_OWNER".equals(ur.getRole().getCode()));

        userRepository.flush(); // 先刪除舊的

        assignStoreRoles(user, roleCodes);
        validateStoreRoleConstraints(user);
    }

    /** 指派多個 STORE 類角色 */
    private void assignStoreRoles(User user, List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) return;

        for (String roleCode : roleCodes) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new ResourceNotFoundException("找不到角色: " + roleCode));

            boolean alreadyHasRole = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getId().equals(role.getId()));

            if (!alreadyHasRole) {
                user.getUserRoles().add(new UserRole(new UserRoleId(user.getId(), role.getId()), user, role));
            }
        }
    }

    /** 驗證 STORE 角色相關限制 */
    private void validateStoreRoleConstraints(User user) {
        ensureNotLastOwner(user.getId());
        ensureUserRoleIfNoStoreRole(user);
    }

    /** 確保至少有一個 OWNER */
    private void ensureNotLastOwner(Long userId) {
        Role ownerRole = roleRepository.findByCode("ROLE_OWNER")
                .orElseThrow(() -> new ResourceNotFoundException("找不到角色 ROLE_OWNER"));

        boolean hasOtherOwner = userRoleRepository.existsOtherOwner(ownerRole.getId(), userId);
        if (!hasOtherOwner) {
            throw new InvalidRequestException("系統至少需要一個 OWNER，不能移除最後一位店長");
        }
    }

    /** 如果沒有 STORE 角色，就補上 ROLE_USER */
    private void ensureUserRoleIfNoStoreRole(User user) {
        boolean hasStoreRole = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getCategory() == RoleCategoryEnum.STORE);

        if (!hasStoreRole) {
            boolean hasUserRole = user.getUserRoles().stream()
                    .anyMatch(ur -> "ROLE_USER".equals(ur.getRole().getCode()));
            if (!hasUserRole) {
                Role defaultRole = roleRepository.findByCode("ROLE_USER")
                        .orElseThrow(() -> new ResourceNotFoundException("找不到角色 ROLE_USER"));
                user.getUserRoles().add(new UserRole(new UserRoleId(user.getId(), defaultRole.getId()), user, defaultRole));
            }
        }
    }
}
