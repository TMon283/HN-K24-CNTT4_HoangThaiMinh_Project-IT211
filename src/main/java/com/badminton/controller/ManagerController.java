package com.badminton.controller;

import com.badminton.constant.BookingStatus;
import com.badminton.dto.response.ApiResponse;
import com.badminton.dto.response.BookingResponse;
import com.badminton.dto.response.CourtImageResponse;
import com.badminton.service.BookingService;
import com.badminton.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final BookingService bookingService;
    private final CourtService courtService;

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookings(
            @RequestParam(required = false, defaultValue = "PENDING") BookingStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookingResponse> bookings = bookingService.getBookingsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @PutMapping("/bookings/{id}/approve")
    public ResponseEntity<ApiResponse<BookingResponse>> approveBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.approveBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking approved successfully", response));
    }

    @PutMapping("/bookings/{id}/reject")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.rejectBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking rejected successfully", response));
    }

    @PostMapping(value = "/courts/{courtId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<CourtImageResponse>>> uploadCourtImages(
            @PathVariable Long courtId,
            @RequestParam("files") List<MultipartFile> files) {
        List<CourtImageResponse> images = courtService.uploadCourtImages(courtId, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Court images uploaded successfully", images));
    }

    @GetMapping("/courts/{courtId}/images")
    public ResponseEntity<ApiResponse<List<CourtImageResponse>>> getCourtImages(@PathVariable Long courtId) {
        List<CourtImageResponse> images = courtService.getCourtImages(courtId);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
}
