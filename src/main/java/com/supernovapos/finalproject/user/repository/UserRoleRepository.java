package com.supernovapos.finalproject.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.auth.model.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

	// 檢查系統有沒有剩下店長角色
    @Query("""
           SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END
           FROM UserRole ur
           WHERE ur.role.id = :roleId
             AND ur.user.id <> :userId
           """)
    boolean existsOtherOwner(@Param("roleId") Integer roleId,
                             @Param("userId") Long userId);

}