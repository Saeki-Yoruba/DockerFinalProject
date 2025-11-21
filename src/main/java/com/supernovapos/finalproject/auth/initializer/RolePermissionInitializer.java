package com.supernovapos.finalproject.auth.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.RolePermission;
import com.supernovapos.finalproject.auth.model.entity.RolePermissionId;
import com.supernovapos.finalproject.auth.repository.PermissionRepository;
import com.supernovapos.finalproject.auth.repository.RolePermissionRepository;
import com.supernovapos.finalproject.auth.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Order(2)
@RequiredArgsConstructor
@Log4j2
public class RolePermissionInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) {
        // 綁定 ADMIN → 全部
    	Role admin = roleRepository.findByCode("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));
        permissionRepository.findAll().forEach(permission -> {
            rolePermissionRepository.findByRoleAndPermission(admin, permission)
                .orElseGet(() -> {
                    RolePermissionId id = new RolePermissionId(admin.getId(), permission.getId());
                    RolePermission rp = new RolePermission();
                    rp.setId(id);
                    rp.setRole(admin);
                    rp.setPermission(permission);
                    RolePermission saved = rolePermissionRepository.save(rp);
                    log.info("Bound {} to {}", permission.getCode(), admin.getCode());
                    return saved;
                });
        });
    }
}
