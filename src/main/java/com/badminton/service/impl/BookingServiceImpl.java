package com.badminton.service.impl;

import com.badminton.constant.BookingStatus;
import com.badminton.constant.PaymentStatus;
import com.badminton.dto.request.BookingCreateRequest;
import com.badminton.dto.response.BookingResponse;
import com.badminton.entity.Booking;
import com.badminton.entity.Court;
import com.badminton.entity.Payment;
import com.badminton.entity.TimeSlot;
import com.badminton.entity.User;
import com.badminton.exception.ConflictException;
import com.badminton.exception.ResourceNotFoundException;
import com.badminton.exception.ValidationException;
import com.badminton.mapper.BookingMapper;
import com.badminton.repository.BookingRepository;
import com.badminton.repository.CourtRepository;
import com.badminton.repository.TimeSlotRepository;
import com.badminton.repository.UserRepository;
import com.badminton.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, Long userId) {
        validateFutureBookingDate(request.getBookingDate());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + request.getCourtId()));

        if (!court.isActive()) {
            throw new ValidationException("Court is not available for booking");
        }

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " + request.getTimeSlotId()));

        if (!timeSlot.isActive()) {
            throw new ValidationException("Time slot is not available");
        }

        if (bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotId(
                request.getCourtId(), request.getBookingDate(), request.getTimeSlotId())) {
            throw new ConflictException("Booking conflict: court is already booked for this date and time slot");
        }

        Payment payment = Payment.builder()
                .amount(court.getPricePerHour())
                .status(PaymentStatus.PENDING)
                .build();

        Booking booking = Booking.builder()
                .user(user)
                .court(court)
                .timeSlot(timeSlot)
                .bookingDate(request.getBookingDate())
                .status(BookingStatus.PENDING)
                .payment(payment)
                .build();

        payment.setBooking(booking);

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingHistory(Long userId, BookingStatus status, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return bookingRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(bookingMapper::toResponse);
    }

    private void validateFutureBookingDate(LocalDate bookingDate) {
        if (!bookingDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Booking date must be in the future");
        }
    }
}
