package com.supernovapos.finalproject.booking.validation;

import java.time.LocalTime;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BusinessTimeValidator implements ConstraintValidator<ValidBusinessTime, LocalTime> {
	@Override
    public void initialize(ValidBusinessTime constraintAnnotation) {
        // 初始化邏輯，如果需要的話
    }

    @Override
    public boolean isValid(LocalTime time, ConstraintValidatorContext context) {
        if (time == null) {
            return true; // 讓 @NotNull 處理空值驗證
        }
        
        // 檢查是否為 30 分鐘間隔 (分鐘只能是 00 或 30)
        int minute = time.getMinute();
        return minute == 0 || minute == 30;
    }
}
