package com.supernovapos.finalproject.booking.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supernovapos.finalproject.booking.dto.CreateBusinessHoursRequest;
import com.supernovapos.finalproject.booking.model.BusinessHours;
import com.supernovapos.finalproject.booking.repository.BusinessHoursRepository;

@Service
public class BusinessHoursService {
	
	@Autowired
	private BusinessHoursRepository businessHoursRepo;
	
	// 驗證營業時間
	private void validateBusinessHours(CreateBusinessHoursRequest request) {
	    if (request.getOpenTime() == null || request.getCloseTime() == null) {
	        throw new IllegalArgumentException("開始時間與結束時間不能為空");
	    }

	    // 阻擋相同時間
	    if (request.getOpenTime().equals(request.getCloseTime())) {
	        throw new IllegalArgumentException("開始時間與結束時間不能相同");
	    }

	    // 必須開始早於結束
	    if (request.getOpenTime().isAfter(request.getCloseTime())) {
	        throw new IllegalArgumentException("開始時間不能晚於結束時間");
	    }

	    // 星期必須在 0-6
	    if (request.getDayOfWeek() == null || request.getDayOfWeek() < 0 || request.getDayOfWeek() > 6) {
	        throw new IllegalArgumentException("dayOfWeek 必須在 0 到 6 之間");
	    }
	}

    // 建立
    public BusinessHours createBusinessHours(CreateBusinessHoursRequest request) {
        // 1. 驗證基本資料（時間正確性、dayOfWeek 範圍等）
        validateBusinessHours(request);

        // 2. 檢查是否有重複或重疊
        List<BusinessHours> existing = businessHoursRepo.findByDayOfWeek(request.getDayOfWeek());
        for (BusinessHours bh : existing) {
            if (timesOverlap(bh.getOpenTime(), bh.getCloseTime(), request.getOpenTime(), request.getCloseTime())) {
                throw new IllegalArgumentException(
                    String.format("星期 %d 已有重疊的營業時間: %s ~ %s", 
                        request.getDayOfWeek(), bh.getOpenTime(), bh.getCloseTime())
                );
            }
        }

        // 3. 建立並儲存
        BusinessHours businessHours = new BusinessHours(
            request.getDayOfWeek(),
            request.getOpenTime(),
            request.getCloseTime(),
            request.getIsActive()
        );

        return businessHoursRepo.save(businessHours);
    }

    
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
    
    

    // 更新
    public BusinessHours updateBusinessHours(Long id, CreateBusinessHoursRequest request) {
        BusinessHours existing = businessHoursRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("找不到指定的營業時間"));

        // 先驗證基本資料（開始時間 < 結束時間）
        validateBusinessHours(request);

        // 檢查同一天是否有重疊時段（排除自己這筆）
        List<BusinessHours> sameDayHours = businessHoursRepo.findByDayOfWeek(request.getDayOfWeek());
        for (BusinessHours bh : sameDayHours) {
            if (!bh.getId().equals(id) && timesOverlap(
                    bh.getOpenTime(), bh.getCloseTime(),
                    request.getOpenTime(), request.getCloseTime())) {
                throw new IllegalArgumentException("與現有營業時段重疊: " 
                    + bh.getOpenTime() + " ~ " + bh.getCloseTime());
            }
        }

        // 更新欄位
        existing.setDayOfWeek(request.getDayOfWeek());
        existing.setOpenTime(request.getOpenTime());
        existing.setCloseTime(request.getCloseTime());
        existing.setIsActive(request.getIsActive());

        return businessHoursRepo.save(existing);
    }

    // 查整週營業時間（依星期排序）
    public List<BusinessHours> findAllBusinessHours() {
        return businessHoursRepo.findAll(Sort.by("dayOfWeek").ascending());
    }

    // 查指定日期的營業時間（支援多時段）
    public Optional<BusinessHours> findBusinessHoursByDateAndTime(LocalDate date, String timeChoice) {
        byte dayOfWeek = convertToByteDayOfWeek(date.getDayOfWeek()); // 使用轉換方法

        List<BusinessHours> hours = businessHoursRepo
            .findByDayOfWeekAndIsActiveTrueOrderByOpenTimeAsc(dayOfWeek); // 按開始時間排序

        if (hours.isEmpty()) {
            return Optional.empty();
        }

        // 解析預約時間，找出對應的營業時段
        String[] parts = timeChoice.split("-");
        if (parts.length == 2) {
            try {
                LocalTime choiceStart = LocalTime.parse(parts[0].trim());

                // 找出包含預約開始時間的營業時段
                return hours.stream()
                    .filter(bh -> !choiceStart.isBefore(bh.getOpenTime()) &&
                                 !choiceStart.isAfter(bh.getCloseTime()))
                    .findFirst();
            } catch (Exception e) {
                // 如果解析失敗，回傳第一個時段
                return Optional.of(hours.get(0));
            }
        }

        // 如果時間格式不正確，回傳第一個時段
        return Optional.of(hours.get(0));
    }

    // 查指定日期的營業時間（原始方法，保持相容性）
    public Optional<BusinessHours> findBusinessHoursByDate(LocalDate date) {
        byte dayOfWeek = convertToByteDayOfWeek(date.getDayOfWeek()); // 使用轉換方法
        
        List<BusinessHours> hours = businessHoursRepo
            .findByDayOfWeekAndIsActiveTrue(dayOfWeek);
        
        return hours.isEmpty() ? Optional.empty() : Optional.of(hours.get(0));
    }

    // 查今天的營業時間
    public Optional<BusinessHours> findTodayBusinessHours() {
        return findBusinessHoursByDate(LocalDate.now());
    }

    // 根據 DayOfWeek 查詢營業時間（實際使用轉換方法的地方）
    public List<BusinessHours> findBusinessHoursByDayOfWeek(DayOfWeek dayOfWeek) {
        byte dayOfWeekByte = convertToByteDayOfWeek(dayOfWeek);
        return businessHoursRepo.findByDayOfWeekAndIsActiveTrueOrderByOpenTimeAsc(dayOfWeekByte);
    }
    
    
    
    @Transactional
    public void deleteBusinessHour(Long id) {
        if (!businessHoursRepo.existsById(id)) {
            throw new IllegalArgumentException("找不到指定的營業時間");
        }
        businessHoursRepo.deleteById(id);
    }
    
    

    // 檢查指定日期是否營業
    public boolean isOpenOn(LocalDate date) {
        return findBusinessHoursByDate(date).isPresent();
    }

    // 取得指定日期的所有營業時段
    public List<BusinessHours> getAllBusinessHoursForDate(LocalDate date) {
        byte dayOfWeek = convertToByteDayOfWeek(date.getDayOfWeek());
        return businessHoursRepo.findByDayOfWeekAndIsActiveTrueOrderByOpenTimeAsc(dayOfWeek);
    }

    // 將 java.time.DayOfWeek 轉成 0=週日, 1=週一 ... 6=週六
    private byte convertToByteDayOfWeek(DayOfWeek dow) {
        int value = dow.getValue(); // 1=MON ... 7=SUN
        return (byte) (value % 7); // 7 % 7 = 0 (週日)
    }
}
