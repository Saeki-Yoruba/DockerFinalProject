package com.supernovapos.finalproject.auth.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.auth.model.dto.PermissionDto;
import com.supernovapos.finalproject.auth.model.dto.RolePermissionsRequest;
import com.supernovapos.finalproject.auth.model.dto.RolePermissionsResponse;
import com.supernovapos.finalproject.auth.model.dto.RolesWithPermissionsResponse;
import com.supernovapos.finalproject.auth.model.entity.Permission;
import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.RolePermission;
import com.supernovapos.finalproject.auth.model.mapper.PermissionMapper;
import com.supernovapos.finalproject.auth.model.mapper.RbacMapper;
import com.supernovapos.finalproject.auth.repository.PermissionRepository;
import com.supernovapos.finalproject.auth.repository.RolePermissionRepository;
import com.supernovapos.finalproject.auth.repository.RoleRepository;
import com.supernovapos.finalproject.auth.service.RbacAdminService;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RbacAdminServiceImpl implements RbacAdminService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RbacMapper rbacMapper;
    private final PermissionMapper permissionMapper;

    @PersistenceContext
    private EntityManager em;
    
    /**
     * 查詢所有角色與其綁定的權限
     */
    @Override
    public RolesWithPermissionsResponse getRolesWithPermissions() {
        List<RolesWithPermissionsResponse.RoleWithPermissions> roles =
                roleRepository.findAllWithPermissions().stream()
                        .map(rbacMapper::toRoleWithPermissions)
                        .toList();
        return new RolesWithPermissionsResponse(roles);
    }

    /**
     * 查詢角色的所有權限
     */
    @Override
    public List<PermissionDto> getPermissionsByRoleId(Integer roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("查詢失敗：角色不存在, id={}", roleId);
                    return new ResourceNotFoundException("角色不存在, id=" + roleId);
                });

        return rolePermissionRepository.findPermissionsByRoleId(role.getId()).stream()
                .map(permissionMapper::toDto)
                .toList();
    }

    /**
     * 覆蓋單一角色的權限綁定
     */
    @Override
    @Transactional
    public RolePermissionsResponse replacePermissions(RolePermissionsRequest req) {
        log.info("覆蓋角色權限，roleCode={}, 權限數={}", req.getRoleCode(), req.getPermissions().size());

        Role role = findRoleOrThrow(req.getRoleCode());
        List<Permission> permissions = validateAndGetPermissions(req.getPermissions());

        resetRolePermissions(role, permissions);

        log.info("覆蓋角色權限綁定完成");
        return RolePermissionsResponse.builder()
                .role(role.getCode())
                .permissions(permissions.stream().map(Permission::getCode).toList())
                .build();
    }

    // ====== Private Helpers ======

    private Role findRoleOrThrow(String roleCode) {
        return roleRepository.findByCode(roleCode)
                .orElseThrow(() -> {
                    log.warn("角色不存在，code={}", roleCode);
                    return new ResourceNotFoundException("角色不存在: " + roleCode);
                });
    }

    private List<Permission> validateAndGetPermissions(List<String> codes) {
        List<Permission> permissions = permissionRepository.findByCodeIn(codes);

        // 找出未知的
        Set<String> foundCodes = permissions.stream().map(Permission::getCode).collect(Collectors.toSet());
        List<String> unknown = codes.stream().filter(c -> !foundCodes.contains(c)).toList();
        if (!unknown.isEmpty()) {
            log.warn("權限代碼不存在: {}", unknown);
            throw new ResourceNotFoundException("權限代碼不存在: " + String.join(", ", unknown));
        }

        // 檢查停用
        List<String> disabled = permissions.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsAvailable()))
                .map(Permission::getCode)
                .toList();
        if (!disabled.isEmpty()) {
            log.warn("權限已停用，無法綁定: {}", disabled);
            throw new InvalidRequestException("權限已停用，無法綁定: " + String.join(", ", disabled));
        }

        return permissions;
    }

    private void resetRolePermissions(Role role, List<Permission> permissions) {
        log.debug("清空角色舊綁定，roleId={}", role.getId());
        rolePermissionRepository.deleteByRoleId(role.getId());

        em.flush();
        em.clear();
        
        List<RolePermission> rolePermissions = permissions.stream()
                .map(p -> rbacMapper.toRolePermission(role, p))
                .toList();
        rolePermissionRepository.saveAll(rolePermissions);

        log.info("角色權限更新完成，role={}, 新綁定數={}", role.getCode(), rolePermissions.size());
    }
}
