package com.supernovapos.finalproject.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.supernovapos.finalproject.product.model.Products;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderItems {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "quantity", nullable = false)
	@Min(value = 1, message = "Quantity must be at least 1")
	private Integer quantity;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Formula("(CAST(quantity AS DECIMAL(10,2)) * unit_price)")
	private BigDecimal subtotal;

	@Column(name = "note", length = 255)
	private String note;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, columnDefinition = "DATETIME2")
	private LocalDateTime createdAt;

//	與orders為多對一關係，並與order_id產生外鍵關聯
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "FK_order_items_order"))
	@ToString.Exclude
	@JsonBackReference("orders-items")
	private Orders orders;
	
//	與products為多對一關係，並與product_id產生外鍵關聯
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "FK_order_items_product"))
	@ToString.Exclude
	private Products products;
}
