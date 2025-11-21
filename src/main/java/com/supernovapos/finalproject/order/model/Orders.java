package com.supernovapos.finalproject.order.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.supernovapos.finalproject.user.model.entity.User;

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
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Orders {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "total_amount", nullable = false)
	private Integer totalAmount ;

	@Column(name = "status", nullable = false, columnDefinition = "BIT")
	private Boolean status = false;

	@Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
	private String note;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, columnDefinition = "DATETIME2")
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2")
	private LocalDateTime updatedAt;

//	與order_group為多對一關聯，並與group_id產生外關聯
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "FK_orders_group"))
	@ToString.Exclude
	@JsonBackReference("ordergroup-orders") 
	private OrderGroup orderGroup;

//	與order_items為一對多關係	
	@OneToMany(mappedBy = "orders", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@JsonManagedReference("orders-items") 
	private List<OrderItems> orderItems;
	
//	與temp_user為多對一關聯，並與temp_user_id產生外關聯	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "temp_user_id", foreignKey = @ForeignKey(name = "FK_orders_temp_user"))
	@ToString.Exclude
	private TempUser tempUser;
	
//	與user為多對一關聯，並與user_id產生外關聯		
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_orders_user"))
	private  User user;
	
	// Orders 便利方法
	public boolean isDraft() {
	    return !this.status;
	}

	public boolean isSubmitted() {
	    return this.status;
	}
}
