package com.supernovapos.finalproject.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDto {
	@NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式錯誤")
	@Schema(description = "電子信箱", example = "ok666@example.com")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 20, message = "密碼長度必須在 6 到 20 字元之間")
    @Schema(description = "密碼", example = "ok123justice")
    private String password;

    @NotBlank(message = "手機不能為空")
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式錯誤")
    @Schema(description = "手機號碼", example = "0987654321")
    private String phone;
}