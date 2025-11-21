package com.supernovapos.finalproject.store.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商店啟用狀態回應")
public record StoreStatusResponse(
    @Schema(description = "是否啟用", example = "true")
    boolean isActive
) {}