package com.supernovapos.finalproject.auth.model.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.supernovapos.finalproject.common.model.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PermissionCategory category;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod = "GET";

    @Column(nullable = false, length = 255)
    private String url;

    @Column(length = 255)
    private String description;

    @Column(name = "is_avaliable", nullable = false)
    private Boolean isAvailable = true;
    
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnoreProperties({"permission"})
    private Set<RolePermission> rolePermissions = new HashSet<>();
    
}
