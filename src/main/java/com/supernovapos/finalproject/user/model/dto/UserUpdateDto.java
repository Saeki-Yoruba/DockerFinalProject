package com.supernovapos.finalproject.user.model.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    @Schema(description = "新密碼", example = "newPass123")
    @Size(min = 6, max = 20, message = "密碼長度必須在 6 到 20 字元之間")
    private String password;

    @Schema(description = "暱稱", example = "Yoruba")
    private String nickname;

    @Schema(description = "大頭貼 Base64", example = "/images/avatar.png")
    private String avatar;

    @Schema(description = "發票載具", example = "/A12345")
    private String invoiceCarrier;

    @Schema(description = "生日", example = "1995-05-20")
    private LocalDate birthdate;
}
