package com.supernovapos.finalproject.auth.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.supernovapos.finalproject.auth.model.dto.PermissionCategoryDto;
import com.supernovapos.finalproject.auth.model.entity.PermissionCategory;
import com.supernovapos.finalproject.auth.model.mapper.PermissionCategoryMapper;
import com.supernovapos.finalproject.auth.repository.PermissionCategoryRepository;
import com.supernovapos.finalproject.auth.service.PermissionCategoryService;
import com.supernovapos.finalproject.common.exception.ConflictException;
import com.supernovapos.finalproject.common.exception.InvalidRequestException;
import com.supernovapos.finalproject.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionCategoryServiceImpl implements PermissionCategoryService {

    private final PermissionCategoryRepository categoryRepository;
    private final PermissionCategoryMapper mapper;

    @Override
    @Transactional
    public PermissionCategoryDto createCategory(String name, String description) {
        if (!StringUtils.hasText(name)) {
            throw new InvalidRequestException("分類名稱不能為空白");
        }
        if (categoryRepository.findByCategoryName(name).isPresent()) {
            throw new ConflictException("分類名稱已存在: " + name);
        }

        PermissionCategory category = new PermissionCategory();
        category.setCategoryName(name.trim());
        category.setDescription(description);
        PermissionCategory saved = categoryRepository.save(category);

        log.info("建立分類成功: id={}, name={}", saved.getId(), saved.getCategoryName());
        return mapper.toDto(saved);
    }

    @Override
    public List<PermissionCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public PermissionCategoryDto getCategory(Integer id) {
        PermissionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission category not found"));
        return mapper.toDto(category);
    }

    @Override
    @Transactional
    public PermissionCategoryDto updateCategory(Integer id, String name, String description) {
        PermissionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission category not found"));

        if (!StringUtils.hasText(name)) {
            throw new InvalidRequestException("分類名稱不能為空白");
        }

        categoryRepository.findByCategoryNameAndIdNot(name, id).ifPresent(existing -> {
            throw new ConflictException("分類名稱已存在: " + name);
        });

        category.setCategoryName(name.trim());
        category.setDescription(description);
        PermissionCategory updated = categoryRepository.save(category);

        log.info("更新分類成功: id={}, name={}", updated.getId(), updated.getCategoryName());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        PermissionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission category not found"));

        if (!category.getPermissions().isEmpty()) {
            log.warn("刪除分類失敗: id={}，底下仍有權限", id);
            throw new InvalidRequestException("分類下仍有權限，無法刪除");
        }

        categoryRepository.delete(category);
        log.info("刪除分類成功: id={}", id);
    }
}

