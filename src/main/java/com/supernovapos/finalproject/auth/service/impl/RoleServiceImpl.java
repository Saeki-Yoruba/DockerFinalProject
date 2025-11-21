package com.supernovapos.finalproject.auth.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.auth.constant.RoleCategoryEnum;
import com.supernovapos.finalproject.auth.model.dto.RoleCreateRequest;
import com.supernovapos.finalproject.auth.model.dto.RoleResponse;
import com.supernovapos.finalproject.auth.model.dto.RoleUpdateRequest;
import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.mapper.RoleMapper;
import com.supernovapos.finalproject.auth.repository.RolePermissionRepository;
import com.supernovapos.finalproject.auth.repository.RoleRepository;
import com.supernovapos.finalproject.auth.service.RoleService;
import com.supernovapos.finalproject.common.exception.ConflictException;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rpRepository;
    private final RoleMapper roleMapper;

    private static final Set<String> SYSTEM_ROLES = Set.of("ROLE_ADMIN", "ROLE_OWNER", "ROLE_STAFF", "ROLE_USER");

    private boolean isSystemRole(Role role) {
        return SYSTEM_ROLES.contains(role.getCode());
    }

    private boolean isSystemRole(String roleCode) {
        return SYSTEM_ROLES.contains(roleCode);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        log.debug("查詢所有角色");
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    public List<RoleResponse> getActiveRolesForAdmin() {
        log.debug("查詢所有啟用角色（排除 ADMIN 類別）");
        return roleRepository.findByIsAvailableTrue().stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    public List<RoleResponse> getActiveStoreRoles() {
        log.debug("查詢所有啟用的 STORE 類角色");
        return roleRepository.findByCategoryAndIsAvailableTrue(RoleCategoryEnum.STORE).stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    public RoleResponse getRoleById(Integer id) {
        log.debug("查詢角色 by id={}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("角色不存在, id={}", id);
                    return new ResourceNotFoundException("角色不存在, id=" + id);
                });
        return roleMapper.toDto(role);
    }

    @Override
    public RoleResponse getRoleByCode(String code) {
        log.debug("查詢角色 by code={}", code);
        Role role = roleRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("角色不存在, code={}", code);
                    return new ResourceNotFoundException("角色不存在, code=" + code);
                });
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleCreateRequest req) {
        log.info("嘗試建立角色, code={}", req.getCode());

        if (isSystemRole(req.getCode())) {
            log.warn("禁止建立系統保留角色, code={}", req.getCode());
            throw new InvalidRequestException("禁止建立系統保留角色: " + req.getCode());
        }

        if (req.getCategory() == RoleCategoryEnum.ADMIN) {
            log.warn("禁止新增 ADMIN 類角色, code={}", req.getCode());
            throw new InvalidRequestException("禁止新增 ADMIN 類角色");
        }

        if (roleRepository.findByCode(req.getCode()).isPresent()) {
            log.warn("角色代碼已存在, code={}", req.getCode());
            throw new ConflictException("角色代碼已存在: " + req.getCode());
        }

        Role role = new Role();
        role.setCode(req.getCode());
        role.setName(req.getName());
        role.setCategory(req.getCategory());

        Role saved = roleRepository.save(role);
        log.info("角色建立成功, id={}, code={}", saved.getId(), saved.getCode());
        return roleMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Integer id, RoleUpdateRequest req) {
        log.info("嘗試更新角色, id={}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("角色不存在, id={}", id);
                    return new ResourceNotFoundException("角色不存在, id=" + id);
                });

        if (isSystemRole(role)) {
            log.warn("禁止修改系統保留角色, code={}", role.getCode());
            throw new InvalidRequestException("禁止修改系統保留角色: " + role.getCode());
        }

        if (req.getCategory() == RoleCategoryEnum.ADMIN) {
            log.warn("禁止將角色類別設為 ADMIN, id={}", id);
            throw new InvalidRequestException("禁止將角色類別設為 ADMIN");
        }

        role.setName(req.getName());
        role.setCategory(req.getCategory());

        Role saved = roleRepository.save(role);
        log.info("角色更新成功, id={}, code={}", saved.getId(), saved.getCode());
        return roleMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoleResponse updateRoleStatus(Integer id, boolean isAvailable) {
        log.info("嘗試更新角色狀態, id={}, isAvailable={}", id, isAvailable);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("角色不存在, id={}", id);
                    return new ResourceNotFoundException("角色不存在, id=" + id);
                });

        if (isSystemRole(role)) {
            log.warn("禁止停用系統保留角色, code={}", role.getCode());
            throw new InvalidRequestException("禁止停用系統保留角色: " + role.getCode());
        }

        role.setIsAvailable(isAvailable);
        Role saved = roleRepository.save(role);
        log.info("角色狀態更新成功, id={}, code={}, isAvailable={}", saved.getId(), saved.getCode(), saved.getIsAvailable());
        return roleMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteRole(Integer roleId) {
        log.info("嘗試刪除角色, id={}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("角色不存在, id={}", roleId);
                    return new ResourceNotFoundException("角色不存在, id=" + roleId);
                });

        if (isSystemRole(role)) {
            log.warn("禁止刪除系統保留角色, code={}", role.getCode());
            throw new InvalidRequestException("禁止刪除系統保留角色: " + role.getCode());
        }

        boolean used = rpRepository.existsByRoleId(roleId);
        if (used) {
            log.warn("角色仍被使用，無法刪除, id={}", roleId);
            throw new InvalidRequestException("角色仍被使用，無法刪除");
        }

        roleRepository.deleteById(roleId);
        log.info("角色刪除成功, id={}", roleId);
    }
}

