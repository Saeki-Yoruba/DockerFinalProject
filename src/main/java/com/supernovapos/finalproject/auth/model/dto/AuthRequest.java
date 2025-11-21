package com.supernovapos.finalproject.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
	@NotBlank(message = "信箱/手機不能為空")
	@Schema(description = "電子信箱 或 手機號碼", example = "admin@example.com")
	private String username;
	
	@NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 20, message = "密碼長度必須在 6 到 20 字元之間")
    @Schema(description = "密碼", example = "123456")
    private String password;
}
