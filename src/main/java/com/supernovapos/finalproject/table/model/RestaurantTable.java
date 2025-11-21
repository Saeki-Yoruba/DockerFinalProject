package com.supernovapos.finalproject.table.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.supernovapos.finalproject.order.model.OrderGroup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "restaurant_table")
@Getter
@Setter
@NoArgsConstructor
public class RestaurantTable {

	/**
	 * 主鍵 ID，自動遞增
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	/**
	 * 桌號（業務主鍵）
	 */
	@Column(name = "table_id", nullable = false, unique = true)
	@NotNull(message = "桌號不能為空")
	private Integer tableId;

	/**
	 * 可容納人數
	 */
	@Column(name = "capacity", nullable = false)
	@NotNull(message = "可容納人數不能為空")
	@Min(value = 1, message = "可容納人數必須大於 0")
	private Integer capacity;

	/**
	 * 桌子形狀
	 */
	@Column(name = "shape", length = 20)
	@Size(max = 20, message = "桌子形狀不能超過 20 個字元")
	private String shape;

	/**
	 * 桌位的 x 軸起始位置
	 */
	@Column(name = "pos_x")
	private Integer posX;

	/**
	 * 桌位的 y 軸起始位置
	 */
	@Column(name = "pos_y")
	private Integer posY;

    /**
     * 桌子狀態
     */
    @Column(name = "is_available", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'empty'")
    @Pattern(regexp = "^(dining|cleaning|booked|empty)$", message = "桌子狀態只能是：用餐、清潔、訂位、空桌")
    private String isAvailable = "empty";

	// 業務建構子
	public RestaurantTable(Integer tableId, Integer capacity, String shape, Integer posX, Integer posY,
			String isAvailable) {
		this.tableId = tableId;
		this.capacity = capacity;
		this.shape = shape;
		this.posX = posX;
		this.posY = posY;
		this.isAvailable = isAvailable;
	}

// Roy新增
	
	/**
	 * 與訂單群組的一對多關係
	 */
	@OneToMany(mappedBy = "table", fetch = FetchType.LAZY)
	@ToString.Exclude
	@JsonIgnore
	private List<OrderGroup> orderGroups;
	
	public static class Status {
        public static final String AVAILABLE = "empty";
        public static final String DINING = "dining";
        public static final String CLEANING = "cleaning";
        public static final String RESERVED = "booked";
    }

    // 改進後的便利方法 - 使用常數
    public boolean isTableAvailable() {
        return Status.AVAILABLE.equals(isAvailable);  // 不再硬編碼
    }

    public boolean isDining() {
        return Status.DINING.equals(isAvailable);
    }

    public boolean isCleaning() {
        return Status.CLEANING.equals(isAvailable);
    }

    public boolean isReserved() {
        return Status.RESERVED.equals(isAvailable);
    }

    // 設定狀態的便利方法也改用常數
    public void setAvailable() {
        this.isAvailable = Status.AVAILABLE;
    }

    public void setDining() {
        this.isAvailable = Status.DINING;
    }

    public void setCleaning() {
        this.isAvailable = Status.CLEANING;
    }

    public void setReserved() {
        this.isAvailable = Status.RESERVED;
    }
    
    // 便利方法：取得桌子基本資訊
    public String getTableInfo() {
        StringBuilder info = new StringBuilder();
        info.append("桌號: ").append(tableId)
            .append(", 容量: ").append(capacity).append("人")
            .append(", 狀態: ").append(isAvailable);
        
        if (shape != null && !shape.trim().isEmpty()) {
            info.append(", 形狀: ").append(shape);
        }
        
        return info.toString();
    }

    // 便利方法：取得桌子位置資訊
    public String getPositionInfo() {
        if (posX != null && posY != null) {
            return String.format("位置: (%d, %d)", posX, posY);
        }
        return "位置: 未設定";
    }

    // 便利方法：檢查桌子是否有進行中的訂單
    public boolean hasActiveOrder() {
        if (orderGroups == null || orderGroups.isEmpty()) {
            return false;
        }
        
        for (OrderGroup orderGroup : orderGroups) {
            if (orderGroup.getStatus()) {
                return true;
            }
        }
        return false;
    }

    // 便利方法：取得目前進行中的訂單群組
    public OrderGroup getCurrentOrderGroup() {
        if (orderGroups == null || orderGroups.isEmpty()) {
            return null;
        }
        
        for (OrderGroup orderGroup : orderGroups) {
            if (orderGroup.getStatus()) {
                return orderGroup;
            }
        }
        return null;
    }

}
