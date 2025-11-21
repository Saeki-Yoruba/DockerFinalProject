package com.supernovapos.finalproject.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.supernovapos.finalproject.auth.model.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 依據權限代碼查詢單一權限
     *
     * @param code 權限代碼
     * @return Optional<Permission>
     */
    Optional<Permission> findByCode(String code);

    /**
     * 查詢所有啟用中的權限，並一次載入分類 (JOIN FETCH)
     * - 常用於後台管理頁面載入完整的分類 + 權限清單
     *
     * @return List<Permission>
     */
    @Query("""
           SELECT p
           FROM Permission p
           JOIN FETCH p.category
           WHERE p.isAvailable = true
           """)
    List<Permission> findActiveWithCategory();

    /**
     * 批次依據多個代碼查詢權限
     *
     * @param codes 權限代碼清單
     * @return List<Permission>
     */
    List<Permission> findByCodeIn(List<String> codes);
}
