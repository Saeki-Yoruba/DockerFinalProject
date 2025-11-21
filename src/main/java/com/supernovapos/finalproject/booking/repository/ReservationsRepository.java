package com.supernovapos.finalproject.booking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.supernovapos.finalproject.booking.model.Reservations;
import com.supernovapos.finalproject.table.model.RestaurantTable;

public interface ReservationsRepository extends JpaRepository<Reservations, Long> {

	@Query("SELECT r FROM Reservations r " + "WHERE (r.phoneNumber = :phoneNumber) " + "AND r.status = 'confirmed' "
			+ "AND r.reservationDate = :reservationDate")
	List<Reservations> findConfirmedByPhoneAndDate(@Param("phoneNumber") String phoneNumber,
			@Param("reservationDate") LocalDate reservationDate);

	/**
	 * 查詢指定日期、時段且狀態為某值的訂位紀錄
	 */
	List<Reservations> findByReservationDateAndTimeChoiceAndStatus(LocalDate reservationDate, String timeChoice,
			String status);

	Page<Reservations> findByReservationDateAndStatus(LocalDate reservationDate, String status, Pageable pageable);

	List<Reservations> findByReservationDateAndStatus(LocalDate reservationDate, String status);


	// 可加上更多條件組合查詢

	Page<Reservations> findByStatus(String status, Pageable pageable);

	Page<Reservations> findByReservationDateAndBookedNameContainingIgnoreCaseAndStatus(LocalDate reservationDate,
			String bookedName, String status, Pageable pageable);

	Page<Reservations> findByBookedNameContainingIgnoreCaseAndStatus(String bookedName, String status,
			Pageable pageable);

	@Query("""
			SELECT r FROM Reservations r
			WHERE r.status = :status
			  AND (:reservationDate IS NULL OR r.reservationDate = :reservationDate)
			  AND (
			        :keyword IS NULL
			        OR LOWER(r.bookedName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			        OR LOWER(r.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
			      )
			""")
	Page<Reservations> searchReservations(@Param("reservationDate") LocalDate reservationDate,
			@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
	
	
	List<Reservations> findByRestaurantTableAndReservationDateAndStatus(
		    RestaurantTable restaurantTable, 
		    LocalDate reservationDate, 
		    String status
		);
	
}
