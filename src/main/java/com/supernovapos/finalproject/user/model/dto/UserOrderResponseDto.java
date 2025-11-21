package com.supernovapos.finalproject.user.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderResponseDto {
	private Long id;               
    private String orderCode;      
    private LocalDateTime createdAt; 
    private Integer totalAmount; 
    private String status;       
    private List<UserOrderItemDto> items; 
}
