package com.supernovapos.finalproject.booking.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.booking.dto.PosCreateReservationRequest;
import com.supernovapos.finalproject.booking.dto.UpdateReservationRequest;
import com.supernovapos.finalproject.booking.model.Reservations;
import com.supernovapos.finalproject.booking.service.ReservationsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/reservations")
@Validated
@Tag(name = "Reservation", description = "APIs for customer booking table")
public class PosReservationsController {
// controller class 都會需要 @Tag
// 方法都需要 @Operation 描述裡要寫 給哪個權限
	// 可參考 AuthController

	@Autowired
	private ReservationsService reservationsService;
	

	

	@Operation(summary = "Making a reservation", description = "Role: Store Staff; Create a new reservation with CreateReservationRequest")
	@PreAuthorize("hasAuthority('POS_CREATE')")
	@PostMapping("/pos/create")
	public ResponseEntity<?> createReservation(@Valid @RequestBody PosCreateReservationRequest request,
	        BindingResult bindingResult) {
	    if (bindingResult.hasErrors()) {
	        Map<String, String> errors = new HashMap<>();
	        bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
	        return ResponseEntity.badRequest().body(errors);
	    }

	    try {
	        Reservations reservation = reservationsService.posAddReservation(request);
	        return ResponseEntity.ok(reservation);
	    } catch (RuntimeException e) {
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("error", "預約失敗");
	        errorResponse.put("message", e.getMessage());
	        return ResponseEntity.badRequest().body(errorResponse);
	    }
	}
	
	
	@Operation(summary = "Update a reservation", description = "Role: Store Staff; Update an existing reservation with UpdateReservationRequest")
	@PreAuthorize("hasAuthority('POS_UPDATE')")
	@PutMapping("/pos/update/{id}")
	public ResponseEntity<?> updateReservation(
	        @PathVariable Long id,
	        @Valid @RequestBody UpdateReservationRequest request,
	        BindingResult bindingResult
	) {
	    // 1. 驗證輸入
	    if (bindingResult.hasErrors()) {
	        Map<String, String> errors = new HashMap<>();
	        bindingResult.getFieldErrors()
	                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
	        return ResponseEntity.badRequest().body(errors);
	    }

	    try {
	        // 2. 呼叫 Service 更新預約
	        Reservations updated = reservationsService.updateReservation(id, request);
	        return ResponseEntity.ok(updated);
	    } catch (RuntimeException e) {
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("error", "更新預約失敗");
	        errorResponse.put("message", e.getMessage());
	        return ResponseEntity.badRequest().body(errorResponse);
	    }
	}


	@Operation(summary = "searching availableTime", description = "Role: Store Staff ; searching availableTime")
	@PreAuthorize("hasAuthority('POS_GETAVAILABLETIME')")
	@GetMapping("/pos/available-time")
	public ResponseEntity<?> getAvailableTime(@RequestParam Integer people,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reservationDate) {
		try {
			List<String> availableSlots = reservationsService.getAvailableTimeSlots(people, reservationDate);
			return ResponseEntity.ok(availableSlots);
		} catch (RuntimeException e) {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "查詢失敗");
			errorResponse.put("message", e.getMessage());
			return ResponseEntity.badRequest().body(errorResponse);
		}
	}

	
	@Operation(summary = "searching reservations", description = "Role: Store Staff ; searching reservations")
	@PreAuthorize("hasAuthority('POS_LIST')")
	@GetMapping("/pos/list")
	public ResponseEntity<Page<Reservations>> getReservations(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reservationDate,
			@RequestParam(required = false) @Size(max = 50) String keyword,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
		Pageable pageable = PageRequest.of(page, size,
				Sort.by("reservationDate").descending().and(Sort.by("timeChoice")));
		Page<Reservations> result = reservationsService.findReservations(reservationDate, keyword, pageable);
		return ResponseEntity.ok(result);
	}
	
	@Operation(summary = "delete reservation", description = "Role: Store Staff ; delete reservation")
	@DeleteMapping("/pos/delete/{id}")
	@PreAuthorize("hasAuthority('POS_DELETE')")
	public ResponseEntity<?> deleteReservationById(@PathVariable Long id) {
	    try {
	        reservationsService.deleteReservation(id);
	        Map<String,String> body = Map.of("message", "刪除成功");
	        return ResponseEntity.ok(body);
	    } catch (IllegalArgumentException e) {
	        // 找不到時進到此 catch，回 404 並帶上錯誤訊息
	        Map<String, String> body = Map.of(
	            "error", "刪除失敗",
	            "message", e.getMessage()
	        );
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	    }
	}
	
	@Operation(summary = "checkin", description = "Role: Store Staff; checkin")
	@PutMapping("/pos/checkin/{id}")
	@PreAuthorize("hasAuthority('POS_CHECKIN')")
	public ResponseEntity<?> checkinReservation(@PathVariable Long id) {
	    try {
	        reservationsService.changeCheckinStatus(id);

	        Map<String, String> body = Map.of("message", "報到成功");
	        return ResponseEntity.ok(body);

	    } catch (IllegalArgumentException e) {
	        e.printStackTrace(); // ✅ Debug 用，印到 console
	        Map<String, String> body = Map.of(
	            "error", "報到失敗",
	            "message", e.getMessage()
	        );
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);

	    } catch (Exception e) {
	        e.printStackTrace(); // ✅ 印完整錯誤 stack trace
	        Map<String, String> body = Map.of(
	            "error", "報到失敗",
	            "message", e.getMessage()
	        );
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	    }
	}


}
