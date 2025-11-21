package com.supernovapos.finalproject.auth.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.auth.model.entity.UserRoleId;
import com.supernovapos.finalproject.auth.repository.RoleRepository;
import com.supernovapos.finalproject.auth.service.UserRoleService;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;
import com.supernovapos.finalproject.common.util.SystemAccountProtector;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;
import com.supernovapos.finalproject.user.model.entity.User;
import com.supernovapos.finalproject.user.model.mapper.UserMapper;
import com.supernovapos.finalproject.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final SystemAccountProtector protector;

    @Override
    @Transactional
    public UserResponseDto replaceUserRoles(Long userId, List<String> roleCodes) {
        log.info("嘗試覆蓋用戶角色, userId={}, 新角色數={}", userId, roleCodes.size());

        User user = findUserOrThrow(userId);

        // SA 保護
        protector.checkNotSystemAdmin(user.getId(), "修改角色");

        // 清空舊角色
        clearUserRoles(user);

        // 新增角色
        addRolesToUser(user, roleCodes);

        // 安全檢查：至少保有一個 OWNER / STAFF / USER
        ensureHasBasicRole(user);

        User saved = userRepository.save(user);
        log.info("覆蓋角色成功, userId={}, 最終角色={}", saved.getId(),
                saved.getUserRoles().stream().map(ur -> ur.getRole().getCode()).toList());

        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoleCodes(Long userId) {
        User user = findUserOrThrow(userId);

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getCode())
                .toList();

        log.debug("查詢用戶角色, userId={}, 角色={}", userId, roles);
        return roles;
    }

    // ===== Private Helpers =====

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("找不到用戶, userId={}", userId);
                    return new ResourceNotFoundException("找不到用戶");
                });
    }

    private void clearUserRoles(User user) {
        user.getUserRoles().clear();
        userRepository.flush(); // 雙主鍵中介表需要同步刪除
        log.debug("清空用戶角色完成, userId={}", user.getId());
    }

    private void addRolesToUser(User user, List<String> roleCodes) {
        for (String roleCode : roleCodes) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> {
                        log.warn("找不到角色, code={}", roleCode);
                        return new ResourceNotFoundException("找不到角色: " + roleCode);
                    });
            user.getUserRoles().add(new UserRole(
                    new UserRoleId(user.getId(), role.getId()), user, role
            ));
        }
        log.debug("新增角色完成, userId={}, 新增角色數={}", user.getId(), roleCodes.size());
    }

    private void ensureHasBasicRole(User user) {
        boolean valid = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getCode())
                .anyMatch(code -> Set.of("ROLE_OWNER", "ROLE_STAFF", "ROLE_USER").contains(code));

        if (!valid) {
            log.warn("用戶缺少基本角色, userId={}", user.getId());
            throw new InvalidRequestException("請至少保留基本角色");
        }
    }
}

