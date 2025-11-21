package com.supernovapos.finalproject.booking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.booking.dto.StoreHolidayRequest;
import com.supernovapos.finalproject.booking.model.StoreHolidays;
import com.supernovapos.finalproject.booking.service.StoreHolidaysService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/storeholidays")
@Tag(name = "StoreHoliday", description = "APIs for shopOwner setting storeholiday")
public class StoreHolidaysController {

	@Autowired
	private StoreHolidaysService holidaysService;

	@Operation(summary = "storeholiday", description = "Role: Store Owner; create a storeholiday")
	@PreAuthorize("hasAuthority('STOREHOLIDAYS_CREATE')")
	@PostMapping("/create")
	public ResponseEntity<?> create(@Valid @RequestBody StoreHolidayRequest dto) {
		try {
			StoreHolidays created = holidaysService.createStoreHoliday(dto);
			// 回傳同一個 Request DTO 作為響應
			StoreHolidayRequest response = new StoreHolidayRequest(created);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

	@Operation(summary = "storeholiday", description = "Role: Store Owner; update a storeholiday")
	@PreAuthorize("hasAuthority('STOREHOLIDAYS_UPDATE')")
	@PutMapping("/update/{id}")
	public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody StoreHolidayRequest dto) {
		try {
			StoreHolidays updated = holidaysService.updateStoreHoliday(id, dto);
			StoreHolidayRequest response = new StoreHolidayRequest(updated);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

	@Operation(summary = "get all holidays", description = "Role: Store Staff; show a list of storeHolidays")
	@PreAuthorize("hasAuthority('STOREHOLIDAYS_LIST')")
	@GetMapping("/list")
	public ResponseEntity<?> getAllStoreHolidays() {
		try {
			List<StoreHolidays> holidays = holidaysService.findAllStoreHolidays();
			return ResponseEntity.ok(holidays);
		} catch (Exception e) {
			// 捕捉任何 RuntimeException，例如資料庫連線失敗
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "取得公休日資料失敗"));
		}
	}

	@Operation(summary = "delete storeholiday", description = "Role: Store Owner; delete a storeholiday")
	@PreAuthorize("hasAuthority('STOREHOLIDAYS_DELETE')")
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		try {
			holidaysService.deleteStoreHoliday(id);
			// 刪除成功，改回傳 200 OK
			return ResponseEntity.ok(Map.of("message", "刪除成功"));
		} catch (IllegalArgumentException e) {
			// 找不到或其他參數錯誤，回 400 並帶錯誤訊息
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

}
