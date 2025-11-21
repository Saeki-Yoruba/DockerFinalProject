package com.supernovapos.finalproject.payment.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.supernovapos.finalproject.user.model.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PointType type;
    
    @Column(name = "points_amount", nullable = false)
    private Integer pointsAmount;
    
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;
    
    @Column(name = "order_group_id")
    private UUID orderGroupId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired = false;
    
    @Column(name = "description", length = 200)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
 // 點數類型枚舉
    public enum PointType {
        ORDER_EARN("order_earn", "消費獲得"),
        ORDER_USE("order_use", "消費使用"),
        ADMIN_GRANT("admin_grant", "管理員給予"),
        ADMIN_DEDUCT("admin_deduct", "管理員扣除"),
        EXPIRED("expired", "點數過期"),
        REFUND("refund", "退款返還");
        
        private final String code;
        private final String description;
        
        PointType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
	
    
	
}
