package com.supernovapos.finalproject.table.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTableInfoRequest {
	private Integer tableId;
	private Integer capacity;
}
