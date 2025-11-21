package com.supernovapos.finalproject.table.Dto;

import com.supernovapos.finalproject.table.model.RestaurantTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class RestaurantTableResponseDto {
	private Integer id;
    private Integer tableId;
    private Integer capacity;
    private String shape;
    private Integer posX;
    private Integer posY;
    private String isAvailable;

    // 其他計算欄位，如果需要可以手動算
    private Boolean tableAvailable;
    private Boolean dining;
    private Boolean reserved;
    private String tableInfo;
    private String positionInfo;

    // Constructor
    public RestaurantTableResponseDto(RestaurantTable table) {
        this.id = table.getId();
        this.tableId = table.getTableId();
        this.capacity = table.getCapacity();
        this.shape = table.getShape();
        this.posX = table.getPosX();
        this.posY = table.getPosY();
        this.isAvailable = table.getIsAvailable();

        this.tableAvailable = "empty".equals(table.getIsAvailable());
        this.dining = "dining".equals(table.getIsAvailable());
        this.reserved = "booked".equals(table.getIsAvailable());
        this.tableInfo = String.format("桌號: %d, 容量: %d人, 狀態: %s, 形狀: %s",
                table.getTableId(), table.getCapacity(), table.getIsAvailable(), table.getShape());
        this.positionInfo = String.format("位置: (%d, %d)", table.getPosX(), table.getPosY());
    }

}
