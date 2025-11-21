package com.supernovapos.finalproject.store.model;

import com.supernovapos.finalproject.common.model.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stores")
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Column(length = 200)
    private String description;

    @Column(name = "logo_url", length = 200)
    private String logoUrl;

    @Column(name = "banner_url", length = 200)
    private String bannerUrl;

    @Column(name = "layout_url", length = 200)
    private String layoutUrl;

    @Column(name = "welcome_message", length = 200)
    private String welcomeMessage;

    @Column(name = "points_per_currency", nullable = false)
    private Double pointsPerCurrency = 100.0;

    @Column(name = "currency_per_point", nullable = false)
    private Double currencyPerPoint = 1.0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
}
