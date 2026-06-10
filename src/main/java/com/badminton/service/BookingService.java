package com.badminton.service;

import com.badminton.dto.request.BookingCreateRequest;
import com.badminton.dto.response.BookingResponse;
import com.badminton.constant.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    BookingResponse createBooking(BookingCreateRequest request, Long userId);

    Page<BookingResponse> getBookingHistory(Long userId, BookingStatus status, Pageable pageable);

    Page<BookingResponse> getBookingsByStatus(BookingStatus status, Pageable pageable);

    BookingResponse approveBooking(Long bookingId);

    BookingResponse rejectBooking(Long bookingId);
}
