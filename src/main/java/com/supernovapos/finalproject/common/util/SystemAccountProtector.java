package com.supernovapos.finalproject.common.util;

import org.springframework.stereotype.Component;

import com.supernovapos.finalproject.common.exception.InvalidRequestException;

@Component
public class SystemAccountProtector {

    private static final long SYSTEM_ADMIN_ID = 1L;

    public void checkNotSystemAdmin(Long userId, String action) {
        if (userId != null && userId == SYSTEM_ADMIN_ID) {
            throw new InvalidRequestException("系統管理員不可執行此操作: " + action);
        }
    }
}
