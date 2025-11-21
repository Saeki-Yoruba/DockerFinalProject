package com.supernovapos.finalproject.user.model.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.common.model.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(name = "phone_number", nullable = false, unique = true, length = 20)
	private String phoneNumber;

	@Column(name = "email_verified", nullable = false)
	private Boolean emailVerified = false;

	@Column(name = "google_uid", length = 255, nullable = true, unique = true)
	private String googleUid;

	@Column(name = "line_uid", length = 255, nullable = true, unique = true)
	private String lineUid;

	@Column(length = 50)
	private String nickname;

	@Lob
	@Column(columnDefinition = "VARCHAR(MAX)")
	private String avatar;

	private LocalDate birthdate;

	@Column(name = "invoice_carrier", length = 20)
	private String invoiceCarrier;

	@Column(nullable = false)
	private Integer point = 0;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnoreProperties({ "user" })
	private Set<UserRole> userRoles = new HashSet<>();
}
