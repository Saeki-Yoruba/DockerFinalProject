package com.supernovapos.finalproject.order.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.supernovapos.finalproject.table.model.RestaurantTable;

import jakarta.persistence.CascadeType;
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
@Table(name = "order_groups")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "uniqueidentifier")
	private UUID id;

	@Column(name = "total_amount", nullable = false)
	private Integer totalAmount;

	@Column(name = "status", nullable = false, columnDefinition = "BIT")
	private Boolean status = true;
	
    @Column(name = "has_order", nullable = false, columnDefinition = "BIT")
    private Boolean hasOrder = false;
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, columnDefinition = "DATETIME2")
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2")
	private LocalDateTime updatedAt;
	
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
	
//	與orders為一對多關係
	@OneToMany(mappedBy = "orderGroup", fetch = FetchType.LAZY)
	@ToString.Exclude
	@JsonManagedReference("ordergroup-orders")
	private List<Orders> orders;
	
//	與RestaurantTable為多對一關係
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "table_id", nullable = false, foreignKey = @ForeignKey(name = "FK_order_groups_table"))
	@JsonIgnore
	private RestaurantTable table;
	
	@OneToMany(mappedBy = "orderGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@ToString.Exclude
	@JsonIgnore
	private List<TempUser> tempUsers;
	
//	「尚未建立任何訂單」且「訂單群組仍是有效的」情況下，才能送出 第一筆訂單。
	public boolean canSubmitFirstOrder() {
		return !this.hasOrder && this.status;
	}
	
//	「已經有訂單」而且「訂單群組還沒關閉」的情況下，才能新增後續的訂單。
	public boolean canAddOrder() {
		return this.hasOrder && this.status;
	}
	
}
