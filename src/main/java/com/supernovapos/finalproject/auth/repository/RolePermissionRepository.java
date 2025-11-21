package com.supernovapos.finalproject.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.auth.model.entity.Permission;
import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.RolePermission;
import com.supernovapos.finalproject.auth.model.entity.RolePermissionId;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

	/**
     * 判斷某角色是否已綁定某權限
     *
     * @param role       角色實體
     * @param permission 權限實體
     * @return Optional<RolePermission>
     */
    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);

    /**
     * 查詢角色目前擁有的所有 Permission 實體
     *
     * @param roleId 角色 ID
     * @return List<Permission>
     */
    @Query("""
           SELECT rp.permission
           FROM RolePermission rp
           WHERE rp.role.id = :roleId
           """)
    List<Permission> findPermissionsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 批次刪除角色的所有權限
     *
     * @param roleId 角色 ID
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Integer roleId);

    /**
     * 判斷是否存在任何綁定在指定角色的權限
     *
     * @param roleId 角色 ID
     * @return true / false
     */
    boolean existsByRoleId(Integer roleId);

    /**
     * 判斷是否存在任何綁定在指定權限的角色
     *
     * @param permissionId 權限 ID
     * @return true / false
     */
    boolean existsByPermissionId(Long permissionId);
}
