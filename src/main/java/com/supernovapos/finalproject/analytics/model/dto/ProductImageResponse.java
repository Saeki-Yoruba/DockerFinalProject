package com.supernovapos.finalproject.analytics.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商品圖片回應物件")
public class ProductImageResponse {

    @Schema(description = "商品 ID", example = "101")
    private Integer productId;

    @Schema(description = "商品名稱", example = "鹽酥雞")
    private String productName;

    @Schema(description = "商品圖片", example = "https://example.com/images/101.jpg")
    private String image;
}
