package com.supernovapos.finalproject.payment.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_group_id")
    private UUID orderGroupId;
    
    @Column(name = "payer_user_id")
    private Long payerUserId;
    
    @Column(name = "merchant_trade_no")
    private String merchantTradeNo;
    
    @Column(name = "trade_no")
    private String tradeNo;
    
    @Column(name = "trade_desc")
    private String tradeDesc;
    
    @Column(name = "payment_type")
    private String paymentType ;
    
    @Column(name = "choose_payment")
    private String choosePayment ;
    
    @Column(name = "trade_status")
    private String tradeStatus;
    
    @Column(name = "total_amount")
    private Integer totalAmount;
    
    @Column(name = "points_used")
    private Integer pointsUsed;
    
    @Column(name = "points_discount")
    private Integer pointsDiscount;
    
    @Column(name = "rtn_code")
    private String rtnCode;
    
    @Column(name = "rtn_msg")
    private String rtnMsg;
    
    @Column(name = "trade_date")
    private String tradeDate;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "simulate_paid")
    private Boolean simulatePaid;
    
    @Column(name = "check_mac_value")
    private String checkMacValue;
    
    @Column(name = "payment_result")
    private String paymentResult;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}