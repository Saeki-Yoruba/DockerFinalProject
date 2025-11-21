package com.supernovapos.finalproject.booking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.supernovapos.finalproject.table.model.RestaurantTable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservations {

    /**
     * 主鍵 ID，自動遞增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 聯絡電話
     */
    @Column(name = "phone_number", length = 20, nullable = false)
    @NotBlank(message = "聯絡電話不能為空")
    @Size(max = 20, message = "聯絡電話不能超過 20 個字元")
    private String phoneNumber;

    /**
     * 信箱（選填）
     */
    @Column(name = "email", length = 100)
    @Email(message = "請輸入有效的電子郵件格式")
    @Size(max = 100, message = "電子郵件不能超過 100 個字元")
    private String email;

    /**
     * 預約人數
     */
    @Column(name = "people", nullable = false)
    @NotNull(message = "預約人數不能為空")
    @Min(value = 1, message = "預約人數必須大於 0")
    private Integer people;

    /**
     * 訂位人名稱
     */
    @Column(name = "booked_name", length = 50, nullable = false)
    @NotBlank(message = "訂位人名稱不能為空")
    @Size(max = 50, message = "訂位人名稱不能超過 50 個字元")
    private String bookedName;

    /**
     * 備註
     */
    @Column(name = "note", length = 500)
    @Size(max = 500, message = "備註不能超過 500 個字元")
    private String note;

    /**
     * 預約狀態
     */
    @Column(name = "status", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'confirmed'")
    @Pattern(regexp = "^(confirmed|cancelled)$", message = "預約狀態只能是 confirmed 或 cancelled")
    private String status = "confirmed";

    /**
     * 建立時間，自動設定
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    /**
     * 預約時段
     */
    @Column(name = "time_choice", length = 50)
    @Pattern(regexp = "^(11:00-12:30|11:30-13:00|12:00-13:30|12:30-14:00|13:00-14:30|13:30-15:00|17:00-18:30|17:30-19:00|18:00-19:30|18:30-20:00|19:00-20:30|19:30-21:00)$",
            message = "請選擇有效的預約時段")
    private String timeChoice;

    /**
     * 是否報到 (預設為 false)
     */
    @Column(name = "checkin_status", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean checkinStatus = false;

    /**
     * 預約日期
     */
    @Column(name = "reservation_date", nullable = false)
    @NotNull(message = "預約日期不能為空")
    private LocalDate reservationDate;

    /**
     * 指定桌號（外鍵關聯）
     */
    @ManyToOne
    @JoinColumn(name = "table_id", referencedColumnName = "table_id")
    private RestaurantTable restaurantTable;

    // 業務建構子
    public Reservations(String phoneNumber, String email, Integer people, String bookedName, 
                       String note, String status, String timeChoice, LocalDate reservationDate) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.people = people;
        this.bookedName = bookedName;
        this.note = note;
        this.status = status;
        this.timeChoice = timeChoice;
        this.reservationDate = reservationDate;
    }

    
}
