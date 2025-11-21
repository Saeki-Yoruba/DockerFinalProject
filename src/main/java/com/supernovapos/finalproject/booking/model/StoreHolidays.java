package com.supernovapos.finalproject.booking.model;


import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_holidays")
@Getter
@Setter
@NoArgsConstructor
public class StoreHolidays {

    /**
     * 主鍵 ID，自動遞增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 特殊公休日
     */
    @Column(name = "holiday_date", nullable = false)
    @NotNull(message = "公休日期不能為空")
    private LocalDate holidayDate;

    /**
     * 是否每年重複 (預設為 false)
     */
    @Column(name = "is_recurring", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isRecurring = false;

    /**
     * 公休原因
     */
    @Column(name = "reason", length = 200)
    @Size(max = 200, message = "公休原因不能超過 200 個字元")
    private String reason;

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

    // 業務建構子
    public StoreHolidays(LocalDate holidayDate, Boolean isRecurring, String reason) {
        this.holidayDate = holidayDate;
        this.isRecurring = isRecurring;
        this.reason = reason;
    }
    
    public StoreHolidays(LocalDate holidayDate) {
        this(holidayDate, false, null);
    }


}
