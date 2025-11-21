package com.supernovapos.finalproject.booking.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.booking.dto.CreateReservationRequest;
import com.supernovapos.finalproject.booking.model.Reservations;
import com.supernovapos.finalproject.booking.service.ReservationsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation", description = "APIs for customer booking table")
public class ReservationsController {
// controller class 都會需要 @Tag
// 方法都需要 @Operation 描述裡要寫 給哪個權限
	// 可參考 AuthController

	@Autowired
	private ReservationsService reservationsService;

	@Autowired
	private SimpMessagingTemplate simp;

	@Operation(summary = "Making a reservation", description = "Role: General Customer; Create a new reservation with CreateReservationRequest")
	@PermitAll
	@PostMapping("/create")
	public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(errors);
		}

		try {
			Reservations reservation = reservationsService.addReservation(request);

			// 廣播給所有訂閱 /topic/booking 的 client
			simp.convertAndSend("/topic/booking", reservation);

			return ResponseEntity.ok(reservation);
		} catch (RuntimeException e) {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "預約失敗");
			errorResponse.put("message", e.getMessage());
			return ResponseEntity.badRequest().body(errorResponse);
		}
	}
	


	@Operation(summary = "searching availableTime", description = "Role: General Customer ; searching availableTime")
	@PermitAll
	@GetMapping("/available-time")
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

}
