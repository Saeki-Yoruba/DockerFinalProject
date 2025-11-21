package com.supernovapos.finalproject.order.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "temp_user")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TempUser {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "uniqueidentifier")
	private UUID id;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "is_register", nullable = false)
	private Boolean isRegister = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, columnDefinition = "DATETIME2")
	private LocalDateTime createdAt;

//	與orders為一對多關係
	@OneToMany(mappedBy = "tempUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@ToString.Exclude
	@JsonIgnore
	private List<Orders> orders ;

// 	與orderGroup為多對一關係
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_group_id", foreignKey = @ForeignKey(name = "FK_temp_user_order_group"))
    @ToString.Exclude
    @JsonIgnore
    private OrderGroup orderGroup;
	
}
