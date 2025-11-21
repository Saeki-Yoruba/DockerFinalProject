package com.supernovapos.finalproject.booking.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_hours")
@Getter
@Setter
@NoArgsConstructor
public class BusinessHours {

    /**
     * 主鍵 ID，自動遞增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 星期幾 (0=週日, 1=週一, 2=週二...6=週六)
     */
    @Column(name = "day_of_week", nullable = false)
    @Min(value = 0, message = "day_of_week 必須在 0-6 之間")
    @Max(value = 6, message = "day_of_week 必須在 0-6 之間")
    @NotNull(message = "day_of_week 不能為空")
    private Byte dayOfWeek;

    /**
     * 營業開始時間
     */
    @Column(name = "open_time", nullable = false)
    @NotNull(message = "營業開始時間不能為空")
    private LocalTime openTime;

    /**
     * 營業結束時間
     */
    @Column(name = "close_time", nullable = false)
    @NotNull(message = "營業結束時間不能為空")
    private LocalTime closeTime;

    /**
     * 是否啟用 (預設為 true)
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;

    /**
     * 建立時間，自動設定
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, 
            columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    /**
     * 更新時間，自動更新
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, 
            columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime updatedAt;


    // 全參數建構子
    public BusinessHours(@Min(value = 0, message = "day_of_week 必須在 0-6 之間") @Max(value = 6, message = "day_of_week 必須在 0-6 之間") @NotNull(message = "day_of_week 不能為空") Byte dayOfWeek, LocalTime openTime, LocalTime closeTime, Boolean isActive) {
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isActive = isActive;
    }

    // 自訂驗證：營業開始時間必須早於結束時間
    @AssertTrue(message = "營業開始時間必須早於結束時間")
    public boolean isValidBusinessHours() {
        if (openTime == null || closeTime == null) {
            return true; // 讓 @NotNull 處理空值驗證
        }
        return openTime.isBefore(closeTime);
    }
    
    public String getDayName() {
        return switch (dayOfWeek) {
            case 0 -> "週日";
            case 1 -> "週一";
            case 2 -> "週二";
            case 3 -> "週三";
            case 4 -> "週四";
            case 5 -> "週五";
            case 6 -> "週六";
            default -> "未知";
        };
    }
    
    public boolean isOpenToday() {
        return isActive != null && isActive;
    }
    
    
    
}