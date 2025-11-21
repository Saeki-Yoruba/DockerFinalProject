package com.supernovapos.finalproject.table.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.table.Dto.RestaurantTableResponseDto;
import com.supernovapos.finalproject.table.Dto.UpdateTablePositionRequest;
import com.supernovapos.finalproject.table.model.RestaurantTable;
import com.supernovapos.finalproject.table.repository.RestaurantTableRepository;


@Service
public class RestaurantTableService {
	
	@Autowired
	private RestaurantTableRepository tableRepository;

	@Transactional
	public RestaurantTable createTable(RestaurantTable table) {
	    if (tableRepository.existsByTableId(table.getTableId())) {
	        throw new IllegalArgumentException("桌號已存在");
	    }

	    // 找到不重疊的位置，自動使用固定邏輯畫布大小
	    int[] pos = findAvailablePosition();
	    table.setPosX(pos[0]);
	    table.setPosY(pos[1]);

	    return tableRepository.save(table);
	}
	
	
	private static final int TABLE_WIDTH = 120;
	private static final int TABLE_HEIGHT = 80;
	private static final int MARGIN = 20;
	private static final int LOGIC_CANVAS_WIDTH = 1600;   // 後端邏輯畫布寬度
	private static final int LOGIC_CANVAS_HEIGHT = 1000;


	
	
	private int[] findAvailablePosition() {
	    List<RestaurantTable> allTables = tableRepository.findAll();
	    int x = 0, y = 0;

	    while (true) {
	        boolean overlap = false;
	        for (RestaurantTable t : allTables) {
	            int leftA = x;
	            int rightA = x + TABLE_WIDTH;
	            int topA = y;
	            int bottomA = y + TABLE_HEIGHT;

	            int leftB = t.getPosX();
	            int rightB = t.getPosX() + TABLE_WIDTH;
	            int topB = t.getPosY();
	            int bottomB = t.getPosY() + TABLE_HEIGHT;

	            boolean isOverlap = !(rightA + MARGIN <= leftB || leftA >= rightB + MARGIN ||
	                                  bottomA + MARGIN <= topB || topA >= bottomB + MARGIN);

	            if (isOverlap) {
	                overlap = true;
	                break;
	            }
	        }

	        if (!overlap) return new int[]{x, y};

	        // 換下一格
	        x += TABLE_WIDTH + MARGIN;
	        if (x + TABLE_WIDTH > LOGIC_CANVAS_WIDTH) {
	            x = 0;
	            y += TABLE_HEIGHT + MARGIN;
	        }

	        // 超過畫布高度 → 直接丟 Exception
	        if (y + TABLE_HEIGHT > LOGIC_CANVAS_HEIGHT) {
	            throw new IllegalStateException("沒有可用空間擺放新桌子，請刪除部分桌子");
	        }
	    }
	}
    
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    
    public Optional<RestaurantTable> getTableById(Integer id) {
        return tableRepository.findById(id);
    }

    
    // 只更新基本狀態
    public RestaurantTable updateTableStatus(Integer id, String isAvailable) {
    	if (!isValidStatus(isAvailable)) {
            throw new IllegalArgumentException("桌子狀態只能是：dining / cleaning / booked / empty");
        }
        RestaurantTable table = getTableOrThrow(id);
        table.setIsAvailable(isAvailable.toLowerCase());
        return tableRepository.save(table);
    }
    
    
    
    // 只更新基本資訊 不移動桌子
    public RestaurantTable updateTableInfo(Integer id, Integer tableId, Integer capacity) {
    	RestaurantTable table = getTableOrThrow(id);

        table.setTableId(tableId);
        table.setCapacity(capacity);
 

        return tableRepository.save(table);
    }
    
    
 

    public void updateLayout(List<UpdateTablePositionRequest> requests) {
        // 先檢查互相之間的碰撞
        for (int i = 0; i < requests.size(); i++) {
            UpdateTablePositionRequest a = requests.get(i);
            for (int j = i + 1; j < requests.size(); j++) {
                UpdateTablePositionRequest b = requests.get(j);

                boolean overlap = !(a.getPosX() + TABLE_WIDTH + MARGIN <= b.getPosX() ||
                                    a.getPosX() >= b.getPosX() + TABLE_WIDTH + MARGIN ||
                                    a.getPosY() + TABLE_HEIGHT + MARGIN <= b.getPosY() ||
                                    a.getPosY() >= b.getPosY() + TABLE_HEIGHT + MARGIN);

                if (overlap) {
                    throw new IllegalArgumentException(
                        "桌子 " + a.getId() + " 與 " + b.getId() + " 新座標重疊"
                    );
                }
            }
        }

        // 檢查完畢後更新資料庫
        List<RestaurantTable> allTables = tableRepository.findAll();
        for (UpdateTablePositionRequest req : requests) {
            RestaurantTable table = allTables.stream()
                    .filter(t -> t.getId().equals(req.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("找不到桌子 id=" + req.getId()));
            table.setPosX(req.getPosX());
            table.setPosY(req.getPosY());
            tableRepository.save(table);
        }
    }


    
    public RestaurantTable updateTablePosition(Integer id, Integer posX, Integer posY) {
        // 先取得資料庫現有桌子
        List<RestaurantTable> allTables = tableRepository.findAll();

        // 只檢查自己與其他桌子的碰撞
        RestaurantTable table = allTables.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到桌子 id=" + id));

        for (RestaurantTable t : allTables) {
            if (t.getId().equals(id)) continue;

            boolean overlap = !(posX + TABLE_WIDTH + MARGIN <= t.getPosX() ||
                                posX >= t.getPosX() + TABLE_WIDTH + MARGIN ||
                                posY + TABLE_HEIGHT + MARGIN <= t.getPosY() ||
                                posY >= t.getPosY() + TABLE_HEIGHT + MARGIN);
            if (overlap) {
                throw new IllegalArgumentException("新座標與其他桌子重疊！");
            }
        }

        table.setPosX(posX);
        table.setPosY(posY);
        return tableRepository.save(table);
    }



    
    public void deleteTable(Integer id) {
        if (!tableRepository.existsById(id)) {
            throw new IllegalArgumentException("找不到要刪除的桌子");
        }
        tableRepository.deleteById(id);
    }
	
    public RestaurantTable findTableByTableId(Integer tableId) {
        return tableRepository.findByTableId(tableId)
                .orElseThrow(() -> new RuntimeException("桌號不存在: " + tableId));
    }
	
    private RestaurantTable getTableOrThrow(Integer id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到指定桌子"));
    }
    
    private static final List<String> VALID_STATUSES = Arrays.asList("dining", "cleaning", "booked", "empty");

    private boolean isValidStatus(String status) {
        return status != null && VALID_STATUSES.contains(status.toLowerCase());
    }
    
    // 找空桌
    public List<RestaurantTableResponseDto> findEmptyTables() {
        List<RestaurantTable> emptyTables = tableRepository.findByIsAvailable("empty");

        return emptyTables.stream()
                .map(RestaurantTableResponseDto::new) // 轉 DTO
                .collect(Collectors.toList());
    }
    
    


}
