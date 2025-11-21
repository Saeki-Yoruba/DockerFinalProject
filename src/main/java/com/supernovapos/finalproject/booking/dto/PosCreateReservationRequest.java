package com.supernovapos.finalproject.booking.dto;

import java.time.LocalDate;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PosCreateReservationRequest {
	@NotBlank(message = "聯絡電話不能為空")
    @Size(max = 20, message = "聯絡電話不能超過 20 個字元")
    private String phoneNumber;

    @Email(message = "請輸入有效的電子郵件格式")
    @Size(max = 100, message = "電子郵件不能超過 100 個字元")
    private String email;

    @NotNull(message = "預約人數不能為空")
    @Min(value = 1, message = "預約人數必須大於 0")
    private Integer people;

    @NotBlank(message = "訂位人名稱不能為空")
    @Size(max = 50, message = "訂位人名稱不能超過 50 個字元")
    private String bookedName;

    @Size(max = 500, message = "備註不能超過 500 個字元")
    private String note;

    @NotNull(message = "預約日期不能為空")
    private LocalDate reservationDate;

    @NotBlank(message = "預約時段不能為空")
    @Pattern(regexp = "^(11:00-12:30|11:30-13:00|12:00-13:30|12:30-14:00|13:00-14:30|13:30-15:00|17:00-18:30|17:30-19:00|18:00-19:30|18:30-20:00|19:00-20:30|19:30-21:00)$",
            message = "請選擇有效的預約時段")
    private String timeChoice;
    
}
