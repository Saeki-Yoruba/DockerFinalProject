package com.supernovapos.finalproject.payment.model.dto;

import java.time.LocalDateTime;

import com.supernovapos.finalproject.payment.model.Point;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointHistoryDTO {
    private Integer id;
    private String type;
    private Integer pointsAmount;
    private Integer balanceAfter;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Boolean isExpired;
    private String orderGroupId;
    
    public static PointHistoryDTO fromEntity(Point point) {
        return PointHistoryDTO.builder()
                .id(point.getId())
                .type(point.getType().name())
                .pointsAmount(point.getPointsAmount())
                .balanceAfter(point.getBalanceAfter())
                .description(point.getDescription())
                .createdAt(point.getCreatedAt())
                .expiredAt(point.getExpiredAt())
                .isExpired(point.getIsExpired())
                .orderGroupId(point.getOrderGroupId() != null ? point.getOrderGroupId().toString() : null)
                .build();
    }
}
