package com.supernovapos.finalproject.auth.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.supernovapos.finalproject.auth.constant.PermissionEnum;
import com.supernovapos.finalproject.auth.model.entity.Permission;
import com.supernovapos.finalproject.auth.model.entity.PermissionCategory;
import com.supernovapos.finalproject.auth.repository.PermissionCategoryRepository;
import com.supernovapos.finalproject.auth.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Order(1)
@RequiredArgsConstructor
@Log4j2
public class PermissionInitializer implements CommandLineRunner {

	private final PermissionRepository permissionRepository;
	private final PermissionCategoryRepository categoryRepository;

	@Override
	public void run(String... args) {
        log.info("Starting PermissionInitializer...");

        for (PermissionEnum p : PermissionEnum.values()) {
            // 1️ Ensure category exists
            PermissionCategory category = categoryRepository.findByCategoryName(p.getCategoryName()).orElseGet(() -> {
                PermissionCategory newCategory = new PermissionCategory();
                newCategory.setCategoryName(p.getCategoryName());
                newCategory.setDescription(p.getCategoryName() + " related permissions");
                PermissionCategory saved = categoryRepository.save(newCategory);
                log.info("Created new category: {}", saved.getCategoryName());
                return saved;
            });

            // 2️ Ensure permission exists
            permissionRepository.findByCode(p.getCode()).orElseGet(() -> {
                Permission newPermission = new Permission();
                newPermission.setCategory(category);
                newPermission.setCode(p.getCode());
                newPermission.setHttpMethod(p.getHttpMethod());
                newPermission.setUrl(p.getUrl());
                newPermission.setDescription(p.getDescription());
                newPermission.setIsAvailable(true);
                Permission saved = permissionRepository.save(newPermission);
                log.info("Created new permission: {} [{} {}]", 
                        saved.getCode(), saved.getHttpMethod(), saved.getUrl());
                return saved;
            });
        }

        log.info("PermissionInitializer completed.");
    }
}