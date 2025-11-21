package com.supernovapos.finalproject.user.model.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "使用者回應資料")
public class UserResponseDto {
	
	@Schema(description = "使用者 ID", example = "101")
    private Long id;

    @Schema(description = "電子郵件", example = "user@example.com")
    private String email;

    @Schema(description = "手機號碼", example = "0912345678")
    private String phoneNumber;

    @Schema(description = "是否驗證 Email", example = "true")
    private boolean emailVerified;

    @Schema(description = "暱稱", example = "小明")
    private String nickname;

    @Schema(description = "大頭貼（URL 或 Base64）", 
            example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "生日", example = "1998-05-21")
    private LocalDate birthdate;

    @Schema(description = "發票載具", example = "/AB123456")
    private String invoiceCarrier;

    private String googleUid;

    private String lineUid;
    
    @Schema(description = "目前點數", example = "250")
    private Integer point;

    @Schema(description = "帳號是否啟用", example = "true")
    private boolean active;

    @Schema(description = "角色清單", 
            example = "[\"ROLE_USER\", \"ROLE_STAFF\"]")
    private List<String> roles;
}