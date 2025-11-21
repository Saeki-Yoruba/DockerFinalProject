package com.supernovapos.finalproject.auth.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.supernovapos.finalproject.auth.constant.RoleEnum;
import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Order(1)
@RequiredArgsConstructor
@Log4j2
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            roleRepository.findByCode(roleEnum.getCode())
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setCode(roleEnum.getCode());
                    role.setName(roleEnum.getName());
                    role.setCategory(roleEnum.getCategory());
                    Role saved = roleRepository.save(role);
                    log.info("Created new role: {}", saved.getCode());
                    return saved;
                });
        }
    }
}