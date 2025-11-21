package com.supernovapos.finalproject.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.supernovapos.finalproject.auth.constant.RoleCategoryEnum;
import com.supernovapos.finalproject.auth.model.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer>{

	/**
     * 依據角色代碼查詢
     *
     * @param code 角色代碼
     * @return Optional<Role>
     */
    Optional<Role> findByCode(String code);

    /**
     * 查詢所有角色，並一次載入綁定的權限 (JOIN FETCH)
     *
     * @return List<Role>
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.rolePermissions")
    List<Role> findAllWithPermissions();

    /**
     * 查詢所有角色（含停用）
     *
     * @return List<Role>
     */
    List<Role> findAll();

    /**
     * 查詢啟用中的角色
     *
     * @return List<Role>
     */
    List<Role> findByIsAvailableTrue();

    /**
     * 查詢指定分類下的啟用角色
     *
     * @param category 角色分類
     * @return List<Role>
     */
    List<Role> findByCategoryAndIsAvailableTrue(RoleCategoryEnum category);
    
}
