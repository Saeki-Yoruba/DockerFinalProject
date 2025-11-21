package com.supernovapos.finalproject.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffRoleResponse {
    private String code;      // 角色代碼，例如 ROLE_STAFF
    private String name;      // 顯示名稱，例如 一般員工
    private boolean assigned; // true=已分配，false=未分配
}
