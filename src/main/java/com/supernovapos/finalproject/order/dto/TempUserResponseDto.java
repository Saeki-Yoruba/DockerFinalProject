package com.supernovapos.finalproject.order.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.supernovapos.finalproject.order.model.TempUser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//TempUser 回應 DTO - 避免序列化問題
@Getter
@Setter
@NoArgsConstructor
public class TempUserResponseDto {
	
	private UUID id;
	private String nickname;
	private Boolean isRegister;
	private LocalDateTime createdAt;
	private UUID orderGroupId; // 只回傳 orderGroup 的 ID，避免整個對象
	
	// 從 TempUser 實體轉換的建構子
	public TempUserResponseDto(TempUser tempUser) {
		this.id = tempUser.getId();
		this.nickname = tempUser.getNickname();
		this.isRegister = tempUser.getIsRegister();
		this.createdAt = tempUser.getCreatedAt();
		
		// 安全地取得 orderGroup ID
		if (tempUser.getOrderGroup() != null) {
			this.orderGroupId = tempUser.getOrderGroup().getId();
		}
	}
	
	// 靜態工廠方法
	public static TempUserResponseDto from(TempUser tempUser) {
		return new TempUserResponseDto(tempUser);
	}
}