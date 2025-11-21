package com.supernovapos.finalproject.table.controller;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.booking.model.Reservations;
import com.supernovapos.finalproject.booking.service.ReservationsService;
import com.supernovapos.finalproject.table.Dto.CreateTableRequest;
import com.supernovapos.finalproject.table.Dto.RestaurantTableResponseDto;
import com.supernovapos.finalproject.table.Dto.UpdateTableInfoRequest;
import com.supernovapos.finalproject.table.Dto.UpdateTablePositionRequest;
import com.supernovapos.finalproject.table.model.RestaurantTable;
import com.supernovapos.finalproject.table.service.RestaurantTableService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/restaurantTable")
@Tag(name = "RestaurantTable", description = "APIs for stroe staff setting restaurant table")
public class RetaurantTableController {
	
	@Autowired
	private RestaurantTableService restaurantTableService;
	
	
	@Autowired
	private ReservationsService  reservationsService;
	
	@Operation(summary = "create restaurant table", description = "Role: Store Owner; create a restaurant table")
	@PreAuthorize("hasAuthority('TABLE_CREATE')")
	@PostMapping("/create")
	public ResponseEntity<?> createTable(@RequestBody CreateTableRequest req) {
	    RestaurantTable table = new RestaurantTable();
	    table.setTableId(req.getTableId());
	    table.setCapacity(req.getCapacity());

	    // 不再傳 canvas 寬高
	    return ResponseEntity.ok(
	        restaurantTableService.createTable(table)
	    );
	}
	
	@Operation(summary = "show restaurant tables", description = "Role: Store staff; show restaurant tables ")
	@PreAuthorize("hasAuthority('TABLE_LIST')")
	@GetMapping("/list")
	public ResponseEntity<?> getAllTables() {
	    List<RestaurantTable> entities = restaurantTableService.getAllTables();
	    if (entities.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("success", false, "message", "找不到任何桌子的資訊"));
	    }

	    List<RestaurantTableResponseDto> dtos = entities.stream()
	            .map(RestaurantTableResponseDto::new)
	            .toList();

	    return ResponseEntity.ok(dtos);
	}
	
	
	@Operation(summary = "get restaurant table by id", description = "Role: Store staff; get restaurant table by id ")
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('TABLE_GETID')")
    public ResponseEntity<?> getTableById(@PathVariable Integer id) {
        try {
            Optional<RestaurantTable> tableOpt = restaurantTableService.getTableById(id);

            if (tableOpt.isEmpty()) {
                // ⚠️ 找不到指定桌子
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "找不到 id = " + id + " 的桌子"
                        ));
            }

            // ✅ 找到桌子，回傳資料
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", tableOpt.get()
            ));

        } catch (Exception e) {
            // ⚠️ 其他未預期錯誤
            e.printStackTrace(); // 除錯用 log
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", e.getClass().getSimpleName(),
                            "message", e.getMessage()
                    ));
        }
    }
	
	@Operation(summary = "get a list of enpty tables", description = "Role: Store staff; get a list of enpty tables")
	@GetMapping("/empty")
	@PreAuthorize("hasAuthority('TABLE_GETEMPTY')")
    public ResponseEntity<?> getEmptyTables() {
        List<RestaurantTableResponseDto> dtos = restaurantTableService.findEmptyTables();

        if (dtos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "目前沒有空桌"));
        }

        return ResponseEntity.ok(Map.of("success", true, "data", dtos));
    }
	
	@Operation(summary = "upadate status of the table", description = "Role: Store staff; upadate status of the table")
	@PreAuthorize("hasAuthority('TABLE_UPDATESTATUS')")
	@PutMapping("/{id}/status")
    public ResponseEntity<?> updateTableIsAvailable(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        try {
            String newStatus = payload.get("isAvailable");
            if (newStatus == null || newStatus.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "缺少 isAvailable"));
            }

            // 呼叫 Service 更新
            RestaurantTable updated = restaurantTableService.updateTableStatus(id, newStatus);

            // 回傳 DTO
            RestaurantTableResponseDto dto = new RestaurantTableResponseDto(updated);
            return ResponseEntity.ok(Map.of("success", true, "data", dto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "更新桌子狀態失敗"));
        }
    }
	
	
	@Operation(summary = "find next reservation", description = "Role: Store staff; show next reservation of the table")
	@PreAuthorize("hasAuthority('TABLE_GETNEXT')")
	@GetMapping("/{tableId}/next")
	public ResponseEntity<?> getNextReservationByTableNumber(@PathVariable Integer tableId) {
	    try {
	        // ✅ 檢查桌子是否存在
	        RestaurantTable table = restaurantTableService.findTableByTableId(tableId);
	        if (table == null) {
	            return ResponseEntity.badRequest()
	                .body(Map.of("error", "桌號不存在", "tableNumber", tableId));
	        }

	        // ✅ 查詢下一筆訂位
	        Reservations nextReservation = reservationsService.findNextReservationForTable(table);

	        // 🔄 用 HashMap，避免 null 造成 Map.of() 錯誤
	        Map<String, Object> body = new HashMap<>();
	        body.put("tableNumber", tableId);

	        if (nextReservation == null) {
	            body.put("message", "今日此桌位無訂位資訊");
	            body.put("reservation", null);
	            body.put("reservationStatus", "無訂位");
	        } else {
	            // 判斷訂位狀態
	            LocalTime now = LocalTime.now();
	            LocalTime[] slot = parseTimeSlot(nextReservation.getTimeChoice());
	            LocalTime start = slot[0], end = slot[1];

	            String reservationStatus;
	            if (!now.isBefore(start) && !now.isAfter(end)) {
	                reservationStatus = "進行中";
	            } else if (start.isAfter(now)) {
	                reservationStatus = "即將開始";
	            } else {
	                reservationStatus = "已結束";
	            }

	            body.put("message", "成功取得訂位資訊");
	            body.put("reservation", nextReservation);
	            body.put("reservationStatus", reservationStatus);
	        }

	        return ResponseEntity.ok(body);

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Map.of("error", "系統錯誤", "message", e.getMessage()));
	    }
	}
	
	
	@Operation(summary = "Update table info", description = "Role: Store staff; update table number and capacity")
	@PreAuthorize("hasAuthority('TABLE_UPDATEINFO')")
	@PutMapping("/{id}/info")
	public ResponseEntity<?> updateTableInfo(
	        @PathVariable Integer id,
	        @RequestBody UpdateTableInfoRequest request) {
	    try {
	    	Integer tableId = request.getTableId();
	    	Integer capacity = request.getCapacity();

	    	if (tableId == null || tableId <= 0 || capacity == null || capacity <= 0) {
	    	    return ResponseEntity.badRequest()
	    	            .body(Map.of("success", false, "message", "tableId 和 capacity 必須大於 0"));
	    	}

	        RestaurantTable updated = restaurantTableService.updateTableInfo(
	                id, request.getTableId(), request.getCapacity());

	        RestaurantTableResponseDto dto = new RestaurantTableResponseDto(updated);
	        return ResponseEntity.ok(Map.of("success", true, "data", dto));

	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest()
	                .body(Map.of("success", false, "message", e.getMessage()));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("success", false, "message", "更新桌子資訊失敗"));
	    }
	}
	
	@Operation(summary = "Update position of the table", description = "Role: Store staff; Update position of the table")
	@PutMapping("/layout")
	@PreAuthorize("hasAuthority('TABLE_UPDATELAYOUT')")
	public ResponseEntity<?> updateLayout(@RequestBody @Valid List<UpdateTablePositionRequest> requests) {
	    restaurantTableService.updateLayout(requests);
	    return ResponseEntity.ok("座標已更新");
	}





    
    // 輔助方法：解析時間區間
    private LocalTime[] parseTimeSlot(String timeChoice) {
        String[] parts = timeChoice.split("-");
        return new LocalTime[]{
            LocalTime.parse(parts[0]),
            LocalTime.parse(parts[1])
        };
    }
	
	
	
}
	
	
	

