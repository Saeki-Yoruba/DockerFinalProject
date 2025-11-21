package com.supernovapos.finalproject.user.model.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "員工註冊請求 DTO")
public class StaffRegisterDto {

    @Schema(description = "員工 Email (必填)", example = "staff@example.com")
    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式錯誤")
    private String email;

    @Schema(description = "登入密碼 (必填，至少 6 碼)", example = "password123")
    @NotBlank(message = "密碼不可為空")
    @Size(min = 6, max = 30, message = "密碼長度需為 6 ~ 30 碼")
    private String password;

    @Schema(description = "電話號碼 (選填)", example = "0911000000")
    private String phoneNumber;

    @Schema(description = "暱稱 (必填)", example = "小明")
    @NotBlank(message = "暱稱不可為空")
    @Size(max = 50, message = "暱稱長度不能超過 50")
    private String nickname;

    @Schema(description = "頭像 URL (選填)", example = "/images/avatar123.png")
    private String avatar;

    @Schema(description = "生日 (選填，必須小於今天)", example = "1995-05-20")
    @Past(message = "生日必須是過去的日期")
    private LocalDate birthdate;

    @Schema(description = "角色代碼清單 (至少一個)", 
            example = "[\"ROLE_STAFF\", \"ROLE_CASHIER\"]")
    @NotEmpty(message = "至少要指定一個角色")
    private List<String> roles;
}