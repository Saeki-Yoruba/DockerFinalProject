package com.supernovapos.finalproject.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.auth.service.UserRoleService;
import com.supernovapos.finalproject.auth.service.impl.UserRoleServiceImpl;
import com.supernovapos.finalproject.user.model.dto.UserResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Role 管理", description = "用戶角色覆蓋 API (限 ADMIN 使用)")
public class UserRoleAdminController {

    private final UserRoleService userRoleService;

    @Operation(summary = "覆蓋用戶角色", description = "清空後重建指定用戶的角色清單", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "請至少保留一個 OWNER 或 USER"),
        @ApiResponse(responseCode = "401", description = "未登入 / JWT 無效"),
        @ApiResponse(responseCode = "403", description = "權限不足"),
        @ApiResponse(responseCode = "404", description = "找不到用戶或角色")
    })
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> replaceUserRoles(
            @PathVariable Long id,
            @RequestBody List<String> roleCodes) {
        return ResponseEntity.ok(userRoleService.replaceUserRoles(id, roleCodes));
    }

    @Operation(summary = "查詢用戶角色", description = "取得指定用戶目前擁有的角色清單", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable Long id) {
        return ResponseEntity.ok(userRoleService.getUserRoleCodes(id));
    }
}
