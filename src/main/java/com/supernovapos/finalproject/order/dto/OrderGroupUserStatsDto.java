package com.supernovapos.finalproject.order.dto;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderGroupUserStatsDto {
	private UUID orderGroupId;
	private Integer totalTempUsers;
	private List<String> tempUserNicknames;
}
