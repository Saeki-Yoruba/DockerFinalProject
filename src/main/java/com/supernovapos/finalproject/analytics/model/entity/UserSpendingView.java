package com.supernovapos.finalproject.analytics.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "v_user_spending")
public class UserSpendingView {
    @Id
    @Column(name = "user_id")
    private Long userId;

    private String nickname;
    private String email;

    @Column(name = "total_spent")
    private BigDecimal totalSpent;

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

}
