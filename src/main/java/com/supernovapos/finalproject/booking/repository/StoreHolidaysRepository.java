package com.supernovapos.finalproject.booking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supernovapos.finalproject.booking.model.StoreHolidays;

public interface StoreHolidaysRepository extends JpaRepository<StoreHolidays, Long> {
	 boolean existsByHolidayDate(LocalDate holidayDate);
	 List<StoreHolidays> findAllByOrderByHolidayDateAsc();
	 
	 boolean existsByHolidayDateAndIdNot(LocalDate holidayDate, Long id);
	 
	 
}
