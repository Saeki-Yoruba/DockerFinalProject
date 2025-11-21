package com.supernovapos.finalproject.booking.dto;

import java.time.LocalTime;

import com.supernovapos.finalproject.booking.validation.ValidBusinessTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CreateBusinessHoursRequest {
	
	@Min(value = 0, message = "day_of_week 必須在 0-6 之間")
    @Max(value = 6, message = "day_of_week 必須在 0-6 之間")
    @NotNull(message = "day_of_week 不能為空")
    private Byte dayOfWeek;

    /**
     * 營業開始時間
     */
    @NotNull(message = "營業開始時間不能為空")
    @ValidBusinessTime(message = "營業開始時間必須是 30 分鐘間隔 (例如: 09:00, 09:30)")
    private LocalTime openTime;

    /**
     * 營業結束時間
     */
    @NotNull(message = "營業結束時間不能為空")
    @ValidBusinessTime(message = "營業結束時間必須是 30 分鐘間隔 (例如: 18:00, 18:30)")
    private LocalTime closeTime;

    /**
     * 是否啟用 (預設為 true)
     */
    private Boolean isActive = true;
}
