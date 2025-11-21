package com.supernovapos.finalproject.table.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTablePositionRequest {
    private Integer id;   // DB 主鍵
    private Integer posX;
    private Integer posY;
}
