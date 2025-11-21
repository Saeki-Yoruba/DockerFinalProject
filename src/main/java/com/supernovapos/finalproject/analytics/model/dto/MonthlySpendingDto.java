package com.supernovapos.finalproject.analytics.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySpendingDto {
    private String month;          // yyyy-MM
    private BigDecimal totalSpent; // 當月總消費
}
