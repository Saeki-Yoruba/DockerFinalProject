package com.supernovapos.finalproject.analytics.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "v_daily_sales")
public class DailySalesView {
    @Id
    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "daily_revenue")
    private BigDecimal dailyRevenue;

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "avg_order_value")
    private BigDecimal avgOrderValue;

}
