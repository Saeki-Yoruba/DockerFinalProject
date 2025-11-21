package com.supernovapos.finalproject.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.supernovapos.finalproject.order.model.OrderItems;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
import lombok.ToString;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Products {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "is_available", nullable = false, columnDefinition = "BIT")
	private Boolean isAvailable = true;

	@Column(name = "image", columnDefinition = "VARCHAR(MAX)")
	private String image;

	@Column(name = "description", length = 255)
	private String description;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, columnDefinition = "DATETIME2(0)")
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2(0)")
	private LocalDateTime updatedAt;
	
//	與order_items為一對多關係
	@OneToMany(mappedBy = "products", fetch = FetchType.LAZY)
	@ToString.Exclude
	@JsonIgnore
	private List<OrderItems> orderItems;

//	與product_category為多對一關係，並與category_id產生外鍵關聯
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "FK_products_category"))
    @ToString.Exclude 
//    @JsonBackReference("category-products")
    private ProductCategory productCategory;
}
