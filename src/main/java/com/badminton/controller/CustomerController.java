package com.badminton.controller;

import com.badminton.constant.BookingStatus;
import com.badminton.dto.request.BookingCreateRequest;
import com.badminton.dto.response.ApiResponse;
import com.badminton.dto.response.BookingResponse;
import com.badminton.service.BookingService;
import com.badminton.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@RequiredArgsConstructor
public class CustomerController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        BookingResponse response = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookingHistory(
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<BookingResponse> bookings = bookingService.getBookingHistory(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }
}
