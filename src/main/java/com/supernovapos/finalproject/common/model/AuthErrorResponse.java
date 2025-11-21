package com.supernovapos.finalproject.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Schema(description = "驗證相關錯誤回應格式")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuthErrorResponse extends ErrorResponse {

    @Schema(description = "自訂錯誤代碼", example = "UNVERIFIED_ACCOUNT")
    private String code;
}
