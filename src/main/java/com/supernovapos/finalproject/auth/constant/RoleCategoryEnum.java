package com.supernovapos.finalproject.auth.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleCategoryEnum {
	ADMIN,  // 系統層級管理角色
    STORE,  // 店家內部角色 (Owner, Staff, Cashier...)
    USER   // 一般使用者
}