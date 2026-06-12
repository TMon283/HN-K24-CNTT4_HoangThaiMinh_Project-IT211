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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
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
        log.info("Creating booking for userId={}, courtId={}, timeSlotId={}, date={}",
                userId, request.getCourtId(), request.getTimeSlotId(), request.getBookingDate());

        validateFutureBookingDate(request.getBookingDate());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + request.getCourtId()));

        if (!court.isActive()) {
            log.warn("Booking failed for userId={}: courtId={} is inactive", userId, request.getCourtId());
            throw new ValidationException("Court is not available for booking");
        }

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " + request.getTimeSlotId()));

        if (!timeSlot.isActive()) {
            log.warn("Booking failed for userId={}: timeSlotId={} is inactive", userId, request.getTimeSlotId());
            throw new ValidationException("Time slot is not available");
        }

        if (bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotId(
                request.getCourtId(), request.getBookingDate(), request.getTimeSlotId())) {
            log.warn("Booking conflict for courtId={}, date={}, timeSlotId={}",
                    request.getCourtId(), request.getBookingDate(), request.getTimeSlotId());
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
        log.info("Booking created successfully: bookingId={}, userId={}, status={}",
                saved.getId(), userId, saved.getStatus());

        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingHistory(Long userId, BookingStatus status, Pageable pageable) {
        log.debug("Fetching booking history for userId={}, status={}", userId, status);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return bookingRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        log.debug("Fetching bookings by status={}", status);
        return bookingRepository.findAllByStatus(status, pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    @Transactional
    public BookingResponse approveBooking(Long bookingId) {
        log.info("Approving booking id={}", bookingId);

        Booking booking = findBookingWithDetails(bookingId);
        validatePendingStatus(booking);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking approved successfully: bookingId={}, status={}", saved.getId(), saved.getStatus());

        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {
        log.info("Rejecting booking id={}", bookingId);

        Booking booking = findBookingWithDetails(bookingId);
        validatePendingStatus(booking);
        booking.setStatus(BookingStatus.CANCELLED);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking rejected successfully: bookingId={}, status={}", saved.getId(), saved.getStatus());

        return bookingMapper.toResponse(saved);
    }

    private Booking findBookingWithDetails(Long bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }

    private void validatePendingStatus(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Booking status change failed for bookingId={}: current status is {}", booking.getId(), booking.getStatus());
            throw new ValidationException("Only PENDING bookings can be approved or rejected");
        }
    }

    private void validateFutureBookingDate(LocalDate bookingDate) {
        if (!bookingDate.isAfter(LocalDate.now())) {
            log.warn("Booking failed: date {} is not in the future", bookingDate);
            throw new ValidationException("Booking date must be in the future");
        }
    }
}
