package com.supernovapos.finalproject.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supernovapos.finalproject.auth.model.entity.PermissionCategory;

public interface PermissionCategoryRepository extends JpaRepository<PermissionCategory, Integer> {

	/**
     * 依據分類名稱查詢分類
     * 
     * @param categoryName 欲查詢的分類名稱
     * @return Optional<PermissionCategory>
     */
    Optional<PermissionCategory> findByCategoryName(String categoryName);

    /**
     * 檢查分類名稱是否已存在（排除指定的 ID）
     * - 常用於更新時避免名稱重複
     *
     * @param name 欲查詢的分類名稱
     * @param id   欲排除的分類 ID
     * @return Optional<PermissionCategory>
     */
    Optional<PermissionCategory> findByCategoryNameAndIdNot(String name, Integer id);

}
