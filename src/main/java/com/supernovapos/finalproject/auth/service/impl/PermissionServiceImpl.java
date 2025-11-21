package com.supernovapos.finalproject.auth.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.auth.model.dto.PermissionCreateRequest;
import com.supernovapos.finalproject.auth.model.dto.PermissionResponse;
import com.supernovapos.finalproject.auth.model.dto.PermissionUpdateRequest;
import com.supernovapos.finalproject.auth.model.entity.Permission;
import com.supernovapos.finalproject.auth.model.entity.PermissionCategory;
import com.supernovapos.finalproject.auth.model.mapper.PermissionMapper;
import com.supernovapos.finalproject.auth.repository.PermissionCategoryRepository;
import com.supernovapos.finalproject.auth.repository.PermissionRepository;
import com.supernovapos.finalproject.auth.repository.RolePermissionRepository;
import com.supernovapos.finalproject.auth.service.PermissionService;
import com.supernovapos.finalproject.common.exception.ConflictException;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

	private final PermissionRepository permissionRepository;
	private final RolePermissionRepository rpRepository;
	private final PermissionMapper permissionMapper;
	private final PermissionCategoryRepository categoryRepository;

	// ====== Public APIs ======

	@Override
	public Page<PermissionResponse> getAllPermissions(Pageable pageable) {
		log.debug("查詢所有權限，分頁參數: {}", pageable);
		return permissionRepository.findAll(pageable)
				.map(permissionMapper::toResponse);
	}

	@Override
	public PermissionResponse getPermissionById(Long id) {
		Permission p = findPermissionOrThrow(id);
		return permissionMapper.toResponse(p);
	}

	@Override
	public List<PermissionResponse> getActivePermissionsForBinding() {
		log.debug("查詢啟用中的權限（含分類）");
		return permissionRepository.findActiveWithCategory().stream()
				.map(permissionMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional
	public PermissionResponse createPermission(PermissionCreateRequest req) {
		log.info("嘗試新增權限，code={}", req.getCode());

		ensureCodeNotExists(req.getCode());
		PermissionCategory category = findCategoryOrThrow(req.getCategoryId());

		Permission permission = permissionMapper.toEntity(req);
		permission.setCategory(category);

		Permission saved = permissionRepository.save(permission);
		log.info("新增權限成功，id={}, code={}", saved.getId(), saved.getCode());

		return permissionMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public PermissionResponse updatePermission(Long id, PermissionUpdateRequest req) {
		log.info("嘗試更新權限，id={}", id);

		Permission permission = findPermissionOrThrow(id);
		PermissionCategory category = findCategoryOrThrow(req.getCategoryId());

		permissionMapper.updateEntityFromDto(req, permission);
		permission.setCategory(category);

		Permission saved = permissionRepository.save(permission);
		log.info("更新權限成功，id={}, code={}", saved.getId(), saved.getCode());

		return permissionMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public PermissionResponse updatePermissionStatus(Long id, boolean isAvailable) {
		Permission permission = findPermissionOrThrow(id);
		permission.setIsAvailable(isAvailable);
		Permission saved = permissionRepository.save(permission);

		log.info("更新權限狀態成功，id={}, isAvailable={}", saved.getId(), saved.getIsAvailable());
		return permissionMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public void deletePermission(Long permissionId) {
		log.info("嘗試刪除權限，id={}", permissionId);

		if (rpRepository.existsByPermissionId(permissionId)) {
			log.warn("刪除失敗：權限仍被角色綁定，id={}", permissionId);
			throw new InvalidRequestException("權限仍被角色綁定，無法刪除");
		}

		permissionRepository.deleteById(permissionId);
		log.info("刪除權限成功，id={}", permissionId);
	}

	// ====== Private Helpers ======

	private Permission findPermissionOrThrow(Long id) {
		return permissionRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("查詢失敗：權限不存在，id={}", id);
					return new ResourceNotFoundException("權限不存在, id=" + id);
				});
	}

	private PermissionCategory findCategoryOrThrow(Integer categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> {
					log.warn("分類不存在，categoryId={}", categoryId);
					return new ResourceNotFoundException("分類不存在, id=" + categoryId);
				});
	}

	private void ensureCodeNotExists(String code) {
		if (permissionRepository.findByCode(code).isPresent()) {
			log.warn("權限代碼已存在，code={}", code);
			throw new ConflictException("權限代碼已存在: " + code);
		}
	}
}
