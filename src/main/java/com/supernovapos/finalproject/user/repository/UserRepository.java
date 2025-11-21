package com.supernovapos.finalproject.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.auth.constant.RoleCategoryEnum;
import com.supernovapos.finalproject.user.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // ========== 基本查詢 ==========
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phone);

    // Email 或 Phone 擇一查詢
    @Query("SELECT u FROM User u WHERE u.email = :input OR u.phoneNumber = :input")
    Optional<User> findByEmailOrPhone(@Param("input") String input);

    // ========== 帶角色與權限 (Authentication / RBAC 用) ==========
    @Query("""
            SELECT DISTINCT u
            FROM User u
            LEFT JOIN FETCH u.userRoles ur
            LEFT JOIN FETCH ur.role r
            LEFT JOIN FETCH r.rolePermissions rp
            LEFT JOIN FETCH rp.permission p
            WHERE u.email = :input OR u.phoneNumber = :input
            """)
    Optional<User> findByEmailOrPhoneWithRoles(@Param("input") String input);

    // ========== 分頁查詢 ==========
    @Query("""
            SELECT DISTINCT u
            FROM User u
            JOIN u.userRoles ur
            JOIN ur.role r
            WHERE r.category = :category
            """)
    Page<User> findAllByRoleCategory(@Param("category") RoleCategoryEnum category, Pageable pageable);

    @Query("""
            SELECT DISTINCT u
            FROM User u
            JOIN u.userRoles ur
            JOIN ur.role r
            WHERE r.code = :code
            """)
    Page<User> findAllByRoleCode(@Param("code") String code, Pageable pageable);

    // ========== 第三方登入 ==========
    boolean existsByGoogleUid(String googleUid);
    boolean existsByLineUid(String lineUid);

    Optional<User> findByGoogleUid(String googleUid);
    Optional<User> findByLineUid(String lineUid);
}

