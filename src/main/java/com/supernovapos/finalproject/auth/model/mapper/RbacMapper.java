package com.supernovapos.finalproject.auth.model.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.supernovapos.finalproject.auth.model.dto.RolePermissionsResponse;
import com.supernovapos.finalproject.auth.model.dto.RolesWithPermissionsResponse;
import com.supernovapos.finalproject.auth.model.entity.Permission;
import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.RolePermission;

@Mapper(componentModel = "spring")
public interface RbacMapper {

    // 把 Role + Permission 實體組合成 RolePermission
    default RolePermission toRolePermission(Role role, Permission permission) {
        return new RolePermission(role, permission);
    }

    // Role → RoleWithPermissions
    @Mapping(source = "code", target = "roleCode")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "rolePermissions", target = "permissions")
    RolesWithPermissionsResponse.RoleWithPermissions toRoleWithPermissions(Role role);

    // 把 rolePermissions 映射成權限代碼清單
    default List<String> mapRolePermissions(Set<RolePermission> rolePermissions) {
        return rolePermissions.stream()
                .map(rp -> rp.getPermission().getCode())
                .toList();
    }
}
