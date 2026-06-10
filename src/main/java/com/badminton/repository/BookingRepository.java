package com.badminton.repository;

import com.badminton.constant.BookingStatus;
import com.badminton.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCourtIdAndBookingDateAndTimeSlotId(Long courtId, LocalDate bookingDate, Long timeSlotId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.court
            JOIN FETCH b.timeSlot
            JOIN FETCH b.user
            LEFT JOIN FETCH b.payment
            WHERE b.user.id = :userId
            AND (:status IS NULL OR b.status = :status)
            """)
    Page<Booking> findByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") BookingStatus status,
                                        Pageable pageable);
}
