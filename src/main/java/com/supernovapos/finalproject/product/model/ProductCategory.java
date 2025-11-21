package com.supernovapos.finalproject.product.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "product_category")
@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "category_name", length = 50, nullable = false, unique = true)
	private String categoryName;

	@Column(name = "is_active", nullable = false, columnDefinition = "BIT")
	private Boolean isActive = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, columnDefinition = "DATETIME2(0)")
	private LocalDateTime createdAt;
	
	@ToString.Exclude
	@OneToMany(mappedBy = "productCategory", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Products> products;

}
