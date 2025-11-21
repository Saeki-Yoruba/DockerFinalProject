package com.supernovapos.finalproject.auth.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
	ADMIN("ROLE_ADMIN", "System Administrator", RoleCategoryEnum.ADMIN),
    OWNER("ROLE_OWNER", "Store Owner", RoleCategoryEnum.STORE),
    STAFF("ROLE_STAFF", "Store Staff", RoleCategoryEnum.STORE),
    USER("ROLE_USER", "General User", RoleCategoryEnum.USER);

    private final String code;
    private final String name;
    private final RoleCategoryEnum category;
}
