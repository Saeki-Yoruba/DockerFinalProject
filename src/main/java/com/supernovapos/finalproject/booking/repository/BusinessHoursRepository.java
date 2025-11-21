package com.supernovapos.finalproject.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supernovapos.finalproject.booking.model.BusinessHours;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Long> {
	
	Optional<BusinessHours> findByDayOfWeekAndIsActive(Byte dayOfWeek, Boolean isActive);
	
	// 根據星期幾查詢營業時間（只取啟用的）
    List<BusinessHours> findByDayOfWeekAndIsActiveTrue(int dayOfWeek);
    
    // 根據星期幾查詢所有營業時間（包含停用的）
    List<BusinessHours> findByDayOfWeek(int dayOfWeek);
    
    // 查詢所有啟用的營業時間
    List<BusinessHours> findByIsActiveTrueOrderByDayOfWeekAsc();
    
    // 查詢特定星期幾的營業時間（按開始時間排序）
    List<BusinessHours> findByDayOfWeekAndIsActiveTrueOrderByOpenTimeAsc(int dayOfWeek);
}
