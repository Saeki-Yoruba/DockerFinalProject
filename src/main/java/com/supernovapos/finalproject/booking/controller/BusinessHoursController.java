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

import com.supernovapos.finalproject.booking.dto.CreateBusinessHoursRequest;
import com.supernovapos.finalproject.booking.model.BusinessHours;
import com.supernovapos.finalproject.booking.service.BusinessHoursService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/businessHours")
@Tag(name = "BusinessHour", description = "APIs for shopOwner setting businessHours")
public class BusinessHoursController {
	
	
	@Autowired
	private BusinessHoursService businessHoursService;
	
	//列出所有營業時間
	@Operation(summary = "businessHour", description = "Role: Store Staff; show a list of businesshour")
	@PreAuthorize("hasAuthority('BUSINESSHOUR_LIST')")
	@GetMapping("/list")
	public ResponseEntity<List<BusinessHours>> getAllBusinessHours() {
		List<BusinessHours> allBusinessHours = businessHoursService.findAllBusinessHours();
		return ResponseEntity.ok(allBusinessHours);
	}
	
	@Operation(summary = "create businessHour", description = "Role: Store Owner; create businesshour")
	@PreAuthorize("hasAuthority('BUSINESSHOUR_CREATE')")
	@PostMapping("/create")
    public ResponseEntity<?> createBusinessHours(@RequestBody @Valid CreateBusinessHoursRequest request) {
        try {
            BusinessHours created = businessHoursService.createBusinessHours(request);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            // 異常處理：通常可以回傳 400 Bad Request
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // 其他未預期錯誤
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "伺服器發生錯誤"));
        }
    }
	
	
	@Operation(summary = "Update business hours", description = "Role: Store Owner; update existing business hours")
	@PutMapping("/update/{id}")
	@PreAuthorize("hasAuthority('BUSINESSHOUR_UPDATE')")
	public ResponseEntity<?> updateBusinessHour(
	        @PathVariable Long id,
	        @Valid @RequestBody CreateBusinessHoursRequest request) {
	    try {
	        BusinessHours updated = businessHoursService.updateBusinessHours(id, request);
	        return ResponseEntity.ok(Map.of(
	            "message", "更新成功",
	            "data", updated
	        ));
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                             .body(Map.of("message", e.getMessage()));
	    }
	}
	
	
	
	@Operation(summary = "delete businessHour", description = "Role: Store Owner; delete")
	@PreAuthorize("hasAuthority('BUSINESSHOUR_DELETE')")
	@DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBusinessHour(@PathVariable Long id) {
        try {
            businessHoursService.deleteBusinessHour(id);
            return ResponseEntity.ok(Map.of("message", "刪除成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("message", e.getMessage()));
        }
    }
	

}
