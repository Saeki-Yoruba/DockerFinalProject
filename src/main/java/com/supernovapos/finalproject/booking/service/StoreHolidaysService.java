package com.supernovapos.finalproject.booking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.booking.dto.StoreHolidayRequest;
import com.supernovapos.finalproject.booking.model.StoreHolidays;
import com.supernovapos.finalproject.booking.repository.StoreHolidaysRepository;

@Service
public class StoreHolidaysService {

	@Autowired
	private StoreHolidaysRepository storeHolidaysRepo;

	@Transactional
	public StoreHolidays createStoreHoliday(StoreHolidayRequest dto) {
	    LocalDate holidayDate = dto.getHolidayDate();
	    Boolean isRecurring   = dto.getIsRecurring();
	    String reason         = dto.getReason();

	    // 1. 檢查 holidayDate
	    if (holidayDate == null) {
	        throw new IllegalArgumentException("holidayDate 不能為空");
	    }

	    // 2. 檢查是否已存在同一天紀錄
	    if (storeHolidaysRepo.existsByHolidayDate(holidayDate)) {
	        throw new IllegalArgumentException("該日期已存在公休日紀錄");
	    }

	    // 3. 處理 isRecurring
	    boolean recurring = isRecurring != null && isRecurring;

	    // 4. 處理 reason 長度
	    if (reason != null && reason.length() > 200) {
	        reason = reason.substring(0, 200);
	    }

	    // 5. 建立 Entity 並存入
	    StoreHolidays holiday = new StoreHolidays(holidayDate, recurring, reason);
	    return storeHolidaysRepo.save(holiday);
	}

	@Transactional
	public StoreHolidays updateStoreHoliday(Long id, StoreHolidayRequest dto) {
	    StoreHolidays holiday = storeHolidaysRepo.findById(id)
	        .orElseThrow(() -> new IllegalArgumentException("指定 ID 的公休日不存在"));

	    LocalDate holidayDate = dto.getHolidayDate();
	    Boolean isRecurring   = dto.getIsRecurring();
	    String reason         = dto.getReason();

	    // 1. 檢查 holidayDate
	    if (holidayDate == null) {
	        throw new IllegalArgumentException("holidayDate 不能為空");
	    }

	    // 2. 如果日期有改，檢查是否與其他紀錄衝突
	    if (!holiday.getHolidayDate().equals(holidayDate)) {
	        if (storeHolidaysRepo.existsByHolidayDateAndIdNot(holidayDate, id)) {
	            throw new IllegalArgumentException("修改後的日期已存在其他公休日紀錄");
	        }
	        holiday.setHolidayDate(holidayDate);
	    }

	    // 3. 更新 isRecurring
	    if (isRecurring != null) {
	        holiday.setIsRecurring(isRecurring);
	    }

	    // 4. 更新 reason，並裁切長度
	    if (reason != null) {
	        if (reason.length() > 200) {
	            reason = reason.substring(0, 200);
	        }
	        holiday.setReason(reason);
	    }

	    return storeHolidaysRepo.save(holiday);
	}

	
	
	@Transactional
    public void deleteStoreHoliday(Long id) {
        if (!storeHolidaysRepo.existsById(id)) {
            throw new IllegalArgumentException("找不到指定的特殊公休日");
        }
        storeHolidaysRepo.deleteById(id);
    }
	
	

	public List<StoreHolidays> findAllStoreHolidays() {
		return storeHolidaysRepo.findAllByOrderByHolidayDateAsc();
    }
	
	public Boolean findStoreHolidaysExist(LocalDate date) {
		return storeHolidaysRepo.existsByHolidayDate(date);
    }
	


	public StoreHolidays toEntity(StoreHolidayRequest dto) {
	    StoreHolidays entity = new StoreHolidays();
	    entity.setHolidayDate(dto.getHolidayDate());
	    entity.setIsRecurring(dto.getIsRecurring());
	    entity.setReason(dto.getReason());
	    return entity;
	}

	
	
	
}
