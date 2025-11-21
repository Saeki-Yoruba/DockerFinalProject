package com.supernovapos.finalproject.booking.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.booking.dto.CreateReservationRequest;
import com.supernovapos.finalproject.booking.dto.PosCreateReservationRequest;
import com.supernovapos.finalproject.booking.dto.UpdateReservationRequest;
import com.supernovapos.finalproject.booking.model.BusinessHours;
import com.supernovapos.finalproject.booking.model.Reservations;
import com.supernovapos.finalproject.booking.repository.ReservationsRepository;
import com.supernovapos.finalproject.table.model.RestaurantTable;
import com.supernovapos.finalproject.table.repository.RestaurantTableRepository;

@Service
public class ReservationsService {
	@Autowired
	private ReservationsRepository reservationRepo;

	@Autowired
	private RestaurantTableRepository tableRepository;

	@Autowired
	private BusinessHoursService businessHoursService;

	@Autowired
	private StoreHolidaysService storeHolidaysService;
	



	@Transactional
	public Reservations addReservation(CreateReservationRequest request) {
		// 查詢是否已存在
		List<Reservations> confirmed = reservationRepo.findConfirmedByPhoneAndDate(request.getPhoneNumber(),
				request.getReservationDate());

		if (!confirmed.isEmpty()) {
			throw new IllegalStateException("您在該日期已有預約記錄");
		}

		if (request.getReservationDate().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("預約日期不能是過去的日期");
		}

		if (Boolean.TRUE.equals(storeHolidaysService.findStoreHolidaysExist(request.getReservationDate()))) {
		    throw new IllegalArgumentException("預約日期不能是店家休息日");
		};

		BusinessHours bh = businessHoursService
				.findBusinessHoursByDateAndTime(request.getReservationDate(), request.getTimeChoice()) // 改為傳入時間
				.orElseThrow(() -> new IllegalArgumentException("選擇的時段不在營業時間內"));

		if (!isTimeChoiceWithinBusinessHours(request.getTimeChoice(), bh)) {
			throw new IllegalArgumentException("選擇的時段不在營業時間內");
		}

		// 找空桌
		RestaurantTable assignedTable = findAvailableTable(request.getReservationDate(), request.getTimeChoice(),
				request.getPeople());

		Reservations reservation = new Reservations(request.getPhoneNumber(), request.getEmail(), request.getPeople(),
				request.getBookedName(), request.getNote(), "confirmed", request.getTimeChoice(),
				request.getReservationDate());
		reservation.setRestaurantTable(assignedTable);

		return reservationRepo.save(reservation);
	}
	
	
	@Transactional
	public Reservations posAddReservation(PosCreateReservationRequest request) {
		// 查詢是否已存在
		List<Reservations> confirmed = reservationRepo.findConfirmedByPhoneAndDate(request.getPhoneNumber(),
				request.getReservationDate());

		if (!confirmed.isEmpty()) {
			throw new IllegalStateException("您在該日期已有預約記錄");
		}

		if (request.getReservationDate().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("預約日期不能是過去的日期");
		}

		if (Boolean.TRUE.equals(storeHolidaysService.findStoreHolidaysExist(request.getReservationDate()))) {
		    throw new IllegalArgumentException("預約日期不能是店家休息日");
		};

		BusinessHours bh = businessHoursService
				.findBusinessHoursByDateAndTime(request.getReservationDate(), request.getTimeChoice()) // 改為傳入時間
				.orElseThrow(() -> new IllegalArgumentException("選擇的時段不在營業時間內"));

		if (!isTimeChoiceWithinBusinessHours(request.getTimeChoice(), bh)) {
			throw new IllegalArgumentException("選擇的時段不在營業時間內");
		}
		
		// 找空桌（檢查重疊）
	    RestaurantTable assignedTable = findAvailableTable(
	            request.getReservationDate(),
	            request.getTimeChoice(),
	            request.getPeople()
	    );

	    Reservations reservation = new Reservations(
	            request.getPhoneNumber(),
	            request.getEmail(),
	            request.getPeople(),
	            request.getBookedName(),
	            request.getNote(),
	            "confirmed",
	            request.getTimeChoice(),
	            request.getReservationDate()
	    );
	    reservation.setRestaurantTable(assignedTable);
		return reservationRepo.save(reservation);
	}
	
	
	@Transactional
	public Reservations updateReservation(Long id, UpdateReservationRequest request) {
	    // 1. 先找出要更新的預約
	    Reservations existing = reservationRepo.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("找不到指定預約"));

	    // 2. 驗證日期
	    if (request.getReservationDate().isBefore(LocalDate.now())) {
	        throw new IllegalArgumentException("預約日期不能是過去的日期");
	    }
	    if (Boolean.TRUE.equals(storeHolidaysService.findStoreHolidaysExist(request.getReservationDate()))) {
	        throw new IllegalArgumentException("預約日期不能是店家休息日");
	    }

	    // 3. 檢查營業時間
	    BusinessHours bh = businessHoursService
	            .findBusinessHoursByDateAndTime(request.getReservationDate(), request.getTimeChoice())
	            .orElseThrow(() -> new IllegalArgumentException("選擇的時段不在營業時間內"));

	    if (!isTimeChoiceWithinBusinessHours(request.getTimeChoice(), bh)) {
	        throw new IllegalArgumentException("選擇的時段不在營業時間內");
	    }

	    // 4. 檢查是否與其他預約衝突（排除自己）
	    List<Reservations> confirmed = reservationRepo.findConfirmedByPhoneAndDate(
	            request.getPhoneNumber(), request.getReservationDate());
	    confirmed.removeIf(r -> r.getId().equals(id));
	    if (!confirmed.isEmpty()) {
	        throw new IllegalStateException("您在該日期已有預約記錄");
	    }

	    // 5. 處理桌子分配 - 修復版本
	    RestaurantTable assignedTable;
	    RestaurantTable originalTable = existing.getRestaurantTable();
	    
	    System.out.println("=== 更新預約桌子分配 ===");
	    System.out.println("原桌子: " + (originalTable != null ? "ID:" + originalTable.getId() + ",容量:" + originalTable.getCapacity() : "null"));
	    System.out.println("新人數: " + request.getPeople());
	    
	    // 檢查原桌子是否符合新的人數要求
	    boolean originalTableSuitable = originalTable != null && 
	                                   originalTable.getCapacity() >= request.getPeople();
	    
	    System.out.println("原桌子是否適合新人數: " + originalTableSuitable);
	    
	    if (originalTableSuitable) {
	        // 原桌子容量足夠，檢查是否有時間衝突
	        List<RestaurantTable> conflictTables = reservationRepo
	                .findByReservationDateAndTimeChoiceAndStatus(
	                        request.getReservationDate(),
	                        request.getTimeChoice(),
	                        "confirmed")
	                .stream()
	                .filter(r -> !r.getId().equals(id)) // 排除當前預約
	                .map(Reservations::getRestaurantTable)
	                .filter(Objects::nonNull)
	                .toList();
	        
	        if (!conflictTables.contains(originalTable)) {
	            // 原桌子沒有衝突，可以保留
	            assignedTable = originalTable;
	            System.out.println("保留原桌子: ID=" + assignedTable.getId() + ", 容量=" + assignedTable.getCapacity());
	        } else {
	            // 原桌子有衝突，需要重新分配
	            assignedTable = findAvailableTableExcluding(
	                    request.getReservationDate(),
	                    request.getTimeChoice(),
	                    id,
	                    request.getPeople()
	            );
	            System.out.println("原桌子有衝突，重新分配: ID=" + assignedTable.getId() + ", 容量=" + assignedTable.getCapacity());
	        }
	    } else {
	        // 原桌子容量不足或為null，必須重新分配
	        assignedTable = findAvailableTableExcluding(
	                request.getReservationDate(),
	                request.getTimeChoice(),
	                id,
	                request.getPeople()
	        );
	        System.out.println("原桌子容量不足，重新分配: ID=" + assignedTable.getId() + ", 容量=" + assignedTable.getCapacity());
	    }
	    
	    // 最終驗證：確保分配的桌子容量足夠
	    if (assignedTable.getCapacity() < request.getPeople()) {
	        throw new IllegalStateException("分配的桌子容量(" + assignedTable.getCapacity() + 
	                                       ")不足以容納" + request.getPeople() + "人");
	    }

	    // 6. 更新欄位
	    existing.setBookedName(request.getBookedName());
	    existing.setPhoneNumber(request.getPhoneNumber());
	    existing.setEmail(request.getEmail());
	    existing.setPeople(request.getPeople());
	    existing.setNote(request.getNote());
	    existing.setReservationDate(request.getReservationDate());
	    existing.setTimeChoice(request.getTimeChoice());
	    existing.setRestaurantTable(assignedTable);
	    
	    System.out.println("=== 最終分配結果 ===");
	    System.out.println("預約ID: " + existing.getId());
	    System.out.println("人數: " + existing.getPeople());
	    System.out.println("桌子ID: " + existing.getRestaurantTable().getId());
	    System.out.println("桌子容量: " + existing.getRestaurantTable().getCapacity());
	    System.out.println("====================");

	    // 7. 儲存更新
	    return reservationRepo.save(existing);
	}

	
	// 查所有訂位
	
	@Transactional(readOnly = true)
	public Page<Reservations> findReservations(LocalDate reservationDate, String keyword, Pageable pageable) {
	    if (reservationDate != null && keyword != null && !keyword.isBlank()) {
	        return reservationRepo.findByReservationDateAndBookedNameContainingIgnoreCaseAndStatus(
	            reservationDate, keyword, "confirmed", pageable);
	    } else if (reservationDate != null) {
	        return reservationRepo.findByReservationDateAndStatus(reservationDate, "confirmed", pageable);
	    } else if (keyword != null && !keyword.isBlank()) {
	        return reservationRepo.searchReservations(reservationDate, keyword, "confirmed", pageable);
	    } else {
	        return reservationRepo.findByStatus("confirmed", pageable);
	    }
	}
	
	
	@Transactional
	public void deleteReservation(Long id) {
		if (!reservationRepo.existsById(id)) {
	        throw new IllegalArgumentException("找不到指定的預約紀錄");
	    }
	    reservationRepo.deleteById(id);
	}
	
	
	@Transactional
	public void changeCheckinStatus(Long id) {
	    Reservations reservation = reservationRepo.findById(id)
	        .orElseThrow(() -> new IllegalArgumentException("找不到指定的預約紀錄"));

	    reservation.setCheckinStatus(true);
	    reservationRepo.save(reservation);
	}

	


	// 查當天已被預約時段

	@Transactional(readOnly = true)
	public List<Reservations> findReservationByDate(LocalDate reservationDate) {
		return reservationRepo.findByReservationDateAndStatus(reservationDate, "confirmed");
	}

	// 列出所有可選擇的時段
	@Transactional(readOnly = true)
	public List<String> getAvailableTimeSlots(int people, LocalDate reservationDate) {
		// 所有合法時段（根據 @Pattern 定義）
		List<String> allTimeChoices = List.of("11:00-12:30", "11:30-13:00", "12:00-13:30", "12:30-14:00", "13:00-14:30",
				"13:30-15:00", "17:00-18:30", "17:30-19:00", "18:00-19:30", "18:30-20:00", "19:00-20:30",
				"19:30-21:00");

		// 只要「table.capacity - people ≤ 2」就算合適
		List<RestaurantTable> suitableTables = tableRepository.findAll().stream()
		    .filter(t -> t.getCapacity() >= people)       // 容量要 ≥ 人數
		    .filter(t -> t.getCapacity() - people <= 2)   // 不允許 capacity 差距超過 2
		    .toList();


		// 該日所有已預約資料
		List<Reservations> reservations = reservationRepo.findByReservationDateAndStatus(reservationDate, "confirmed");

		// 回傳可用時段
		List<String> availableSlots = new ArrayList<>();

		for (String timeChoice : allTimeChoices) {
			// 檢查是否在營業時間內
			BusinessHours bh = businessHoursService.findBusinessHoursByDateAndTime(reservationDate, timeChoice)
					.orElse(null);

			if (!isTimeChoiceWithinBusinessHours(timeChoice, bh))
				continue;
			
			// 5. 解析當前候選時段
	        LocalTime[] current = parseTimeSlot(timeChoice);
	        LocalTime currStart = current[0], currEnd = current[1];

			
	     // 6. 找出所有「與當前時段有重疊」的預約桌子
	        List<RestaurantTable> conflictTables = reservations.stream()
	            .filter(r -> {
	                // 避免 r.getTimeChoice() 為 null 或格式錯
	                if (r.getTimeChoice() == null || !r.getTimeChoice().contains("-")) {
	                    return false;
	                }
	                LocalTime[] slot = parseTimeSlot(r.getTimeChoice());
	                return isTimeOverlap(currStart, currEnd, slot[0], slot[1]);
	            })
	            .map(Reservations::getRestaurantTable)
	            .filter(Objects::nonNull)
	            .toList();


			// 該時段剩下的可用桌子（符合人數條件）
	        long availableCount = suitableTables.stream()
	                .filter(t -> !conflictTables.contains(t))
	                .count();


			if (availableCount > 0) {
				availableSlots.add(timeChoice);
			}
		}

		return availableSlots;
	}

	/**
	 * 專門找出某日期、時段的一張可用桌子
	 */
	private RestaurantTable findAvailableTable(LocalDate date, String timeChoice, Integer people) {
	    // 添加輸入驗證和日誌
	    if (people == null || people <= 0) {
	        throw new IllegalArgumentException("人數必須大於0");
	    }
	    
	    System.out.println("尋找可用桌子 - 日期: " + date + ", 時段: " + timeChoice + ", 人數: " + people);
	    
	    List<RestaurantTable> allTables = tableRepository.findAll();
	    System.out.println("總桌子數: " + allTables.size());
	    
	    // 解析當前時段
	    LocalTime[] currentSlot = parseTimeSlot(timeChoice);
	    LocalTime currentStart = currentSlot[0];
	    LocalTime currentEnd = currentSlot[1];
	    
	    // 找出該日期所有確認的預約
	    List<Reservations> allConfirmedReservations = reservationRepo
	        .findByReservationDateAndStatus(date, "confirmed");
	    System.out.println("已確認預約數: " + allConfirmedReservations.size());
	    
	    // 篩選出與當前時段重疊的預約桌
	    Set<RestaurantTable> conflictTables = allConfirmedReservations.stream()
	        .filter(reservation -> {
	            LocalTime[] reservedSlot = parseTimeSlot(reservation.getTimeChoice());
	            return isTimeOverlap(currentStart, currentEnd, reservedSlot[0], reservedSlot[1]);
	        })
	        .map(Reservations::getRestaurantTable)
	        .filter(Objects::nonNull)
	        .collect(Collectors.toSet());
	    
	    System.out.println("衝突桌子數: " + conflictTables.size());
	    
	    // 先篩選出沒有衝突的桌子
	    List<RestaurantTable> availableTables = allTables.stream()
	        .filter(t -> !conflictTables.contains(t))
	        .collect(Collectors.toList());
	    
	    System.out.println("可用桌子數: " + availableTables.size());
	    
	    // 再篩選出符合容量的桌子並添加詳細日誌
	    List<RestaurantTable> suitableTables = availableTables.stream()
	        .peek(t -> System.out.println("檢查桌子 ID: " + t.getId() + 
	                                    ", 容量: " + t.getCapacity() + 
	                                    ", 需求人數: " + people + 
	                                    ", 符合條件: " + (t.getCapacity() >= people)))
	        .filter(t -> {
	            // 明確的容量檢查，確保數據類型正確
	            Integer tableCapacity = t.getCapacity();
	            Integer requiredPeople = people;
	            boolean suitable = tableCapacity != null && requiredPeople != null && 
	                             tableCapacity.intValue() >= requiredPeople.intValue();
	            return suitable;
	        })
	        .sorted(Comparator.comparingInt(RestaurantTable::getCapacity))
	        .collect(Collectors.toList());
	    
	    System.out.println("符合容量要求的桌子數: " + suitableTables.size());
	    
	    if (suitableTables.isEmpty()) {
	        // 提供更詳細的錯誤信息
	        StringBuilder errorMsg = new StringBuilder("該時段沒有可用桌子（符合人數 " + people + "）。");
	        errorMsg.append("可用桌子容量：");
	        availableTables.forEach(t -> errorMsg.append(t.getCapacity()).append("人,"));
	        throw new IllegalStateException(errorMsg.toString());
	    }
	    
	    RestaurantTable selectedTable = suitableTables.get(0);
	    System.out.println("選中桌子 ID: " + selectedTable.getId() + ", 容量: " + selectedTable.getCapacity());
	    
	    return selectedTable;
	}


	// 解析時段字串 "17:30-19:00" -> [LocalTime, LocalTime]
	private LocalTime[] parseTimeSlot(String timeChoice) {
	    String[] parts = timeChoice.split("-");
	    return new LocalTime[]{
	        LocalTime.parse(parts[0]),
	        LocalTime.parse(parts[1])
	    };
	}

	// 檢查兩個時段是否重疊
	private boolean isTimeOverlap(
		    LocalTime start1, LocalTime end1,
		    LocalTime start2, LocalTime end2
		) {
		    // 情況 A：第一段結束在或等於第二段開始 → 無重疊
		    if (!start2.isBefore(end1)) {
		        return false;
		    }
		    // 情況 B：第二段結束在或等於第一段開始 → 無重疊
		    if (!start1.isBefore(end2)) {
		        return false;
		    }
		    // 以上都不是，才是真的有重疊
		    return true;
		}

	
	
	


	// 確認指定時間在營業時間內
	public static boolean isTimeChoiceWithinBusinessHours(String timeChoice, BusinessHours bh) {
		if (timeChoice == null || bh == null)
			return false;

		// 拆成開始和結束時間
		String[] parts = timeChoice.split("-");
		if (parts.length != 2)
			return false;

		try {
			LocalTime choiceStart = LocalTime.parse(parts[0].trim());
			LocalTime choiceEnd = LocalTime.parse(parts[1].trim());

			// 判斷是否完全在營業時間內
			return !choiceStart.isBefore(bh.getOpenTime()) && !choiceEnd.isAfter(bh.getCloseTime());
		} catch (DateTimeParseException e) {
			return false; // 如果格式不正確，直接判定無效
		}
	}

	// 輔助方法：找空桌（排除指定預約）
	private RestaurantTable findAvailableTableExcluding(
	        LocalDate date,
	        String timeChoice,
	        Long excludeReservationId,
	        Integer people
	) {
	    // 添加輸入驗證和日誌
	    if (people == null || people <= 0) {
	        throw new IllegalArgumentException("人數必須大於0");
	    }
	    
	    System.out.println("尋找可用桌子(排除預約ID: " + excludeReservationId + ") - 日期: " + date + 
	                      ", 時段: " + timeChoice + ", 人數: " + people);
	    
	    LocalTime[] targetSlot = parseTimeSlot(timeChoice);
	    LocalTime targetStart = targetSlot[0];
	    LocalTime targetEnd = targetSlot[1];
	    
	    List<RestaurantTable> allTables = tableRepository.findAll();
	    
	    // 取得同日已確認的預約，排除當前預約
	    List<Reservations> confirmed = reservationRepo
	            .findByReservationDateAndStatus(date, "confirmed")
	            .stream()
	            .filter(r -> !r.getId().equals(excludeReservationId))
	            .collect(Collectors.toList());
	    
	    System.out.println("已確認預約數(排除當前): " + confirmed.size());
	    
	    // 找出與當前時段衝突的桌
	    Set<RestaurantTable> conflictTables = confirmed.stream()
	            .filter(r -> r.getRestaurantTable() != null)
	            .filter(r -> {
	                LocalTime[] slot = parseTimeSlot(r.getTimeChoice());
	                return isTimeOverlap(targetStart, targetEnd, slot[0], slot[1]);
	            })
	            .map(Reservations::getRestaurantTable)
	            .collect(Collectors.toSet());
	    
	    System.out.println("衝突桌子數: " + conflictTables.size());
	    
	    // 先篩選出沒有衝突的桌子
	    List<RestaurantTable> availableTables = allTables.stream()
	            .filter(t -> !conflictTables.contains(t))
	            .collect(Collectors.toList());
	    
	    // 再篩選出符合容量的桌子
	    List<RestaurantTable> suitableTables = availableTables.stream()
	            .peek(t -> System.out.println("檢查桌子 ID: " + t.getId() + 
	                                        ", 容量: " + t.getCapacity() + 
	                                        ", 需求人數: " + people + 
	                                        ", 符合條件: " + (t.getCapacity() >= people)))
	            .filter(t -> {
	                Integer tableCapacity = t.getCapacity();
	                Integer requiredPeople = people;
	                return tableCapacity != null && requiredPeople != null && 
	                       tableCapacity.intValue() >= requiredPeople.intValue();
	            })
	            .sorted(Comparator.comparingInt(RestaurantTable::getCapacity))
	            .collect(Collectors.toList());
	    
	    if (suitableTables.isEmpty()) {
	        StringBuilder errorMsg = new StringBuilder("該時段沒有可用桌子（符合人數 " + people + "）。");
	        errorMsg.append("可用桌子容量：");
	        availableTables.forEach(t -> errorMsg.append(t.getCapacity()).append("人,"));
	        throw new IllegalStateException(errorMsg.toString());
	    }
	    
	    RestaurantTable selectedTable = suitableTables.get(0);
	    System.out.println("選中桌子 ID: " + selectedTable.getId() + ", 容量: " + selectedTable.getCapacity());
	    
	    return selectedTable;
	}
	
	
	@Transactional(readOnly = true)
	public Reservations findNextReservationForTable(RestaurantTable table) {
	    LocalDate today = LocalDate.now();
	    LocalTime now = LocalTime.now();
	    
	    List<Reservations> todayReservations = reservationRepo
	            .findByRestaurantTableAndReservationDateAndStatus(table, today, "confirmed");
	    
	    Reservations currentReservation = null;
	    Reservations nextReservation = null;
	    LocalTime nextStartTime = null;
	    
	    for (Reservations r : todayReservations) {
	        LocalTime[] slot = parseTimeSlot(r.getTimeChoice());
	        LocalTime start = slot[0], end = slot[1];
	        
	        // 檢查是否正在進行中
	        if (!now.isBefore(start) && !now.isAfter(end)) {
	            currentReservation = r;
	        }
	        // 檢查是否是未來最近的訂位
	        else if (start.isAfter(now)) {
	            if (nextReservation == null || start.isBefore(nextStartTime)) {
	                nextReservation = r;
	                nextStartTime = start;
	            }
	        }
	    }
	    
	    // 優先返回正在進行中的，否則返回下一筆
	    return currentReservation != null ? currentReservation : nextReservation;
	}
	
	
	
}
