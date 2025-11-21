package com.supernovapos.finalproject.analytics.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

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
@Table(name = "v_group_orders")
public class GroupOrdersView {
    @Id
    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "table_id")
    private Integer tableId;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "total_revenue")
    private Integer totalRevenue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "dining_duration")
    private Integer diningDuration;

}
