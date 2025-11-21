package com.supernovapos.finalproject.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台商店資訊回應 DTO")
public class StoreResponseDto {

    @Schema(description = "商店名稱", example = "丸竹儀")
    private String name;

    @Schema(description = "商店簡介", example = "提供美味創意料理，結合智慧點餐服務")
    private String description;

    @Schema(description = "商店 Logo 圖片 URL", example = "/images/logo.png")
    private String logoUrl;

    @Schema(description = "商店 Banner 圖片 URL", example = "/images/banner.jpg")
    private String bannerUrl;

    @Schema(description = "歡迎訊息", example = "歡迎光臨丸竹儀，祝您用餐愉快")
    private String welcomeMessage;

    @Schema(description = "商店電話", example = "02-123-1234")
    private String phone;

    @Schema(description = "商店地址", example = "106 台北市大安區復興南路一段390號2樓")
    private String address;
}