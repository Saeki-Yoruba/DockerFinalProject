package com.supernovapos.finalproject.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "POS API",
                version = "1.0",
                description = "期末專題 API 文件"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
	    return new OpenAPI()
	        .components(new Components()
	            // Schema 註冊 (不用 .implementation())
	            .addSchemas("ErrorResponse",
	                new io.swagger.v3.oas.models.media.Schema<>().$ref("#/components/schemas/ErrorResponse"))

	            // 通用 Response
	            .addResponses("BadRequestResponse", new ApiResponse()
	                .description("輸入格式錯誤 / 驗證失敗")
	                .content(jsonError()))
	            .addResponses("UnauthorizedResponse", new ApiResponse()
	                .description("未登入 / JWT 無效")
	                .content(jsonError()))
	            .addResponses("ForbiddenResponse", new ApiResponse()
	                .description("權限不足")
	                .content(jsonError()))
	            .addResponses("NotFoundResponse", new ApiResponse()
	                .description("資源不存在")
	                .content(jsonError()))
	            .addResponses("ConflictResponse", new ApiResponse()
	                .description("衝突 / 重複建立")
	                .content(jsonError()))
	            .addResponses("ServerErrorResponse", new ApiResponse()
	                .description("系統錯誤")
	                .content(jsonError()))
	        );
	}

	private Content jsonError() {
	    return new Content().addMediaType("application/json",
	        new MediaType().schema(
	            new io.swagger.v3.oas.models.media.Schema<>().$ref("#/components/schemas/ErrorResponse")
	        ));
	}
}
