package com.badminton.service.impl;

import com.badminton.constant.BookingStatus;
import com.badminton.constant.RoleType;
import com.badminton.dto.request.BookingCreateRequest;
import com.badminton.dto.response.BookingResponse;
import com.badminton.entity.Booking;
import com.badminton.entity.Court;
import com.badminton.entity.Role;
import com.badminton.entity.TimeSlot;
import com.badminton.entity.User;
import com.badminton.exception.ConflictException;
import com.badminton.exception.ValidationException;
import com.badminton.mapper.BookingMapper;
import com.badminton.repository.BookingRepository;
import com.badminton.repository.CourtRepository;
import com.badminton.repository.TimeSlotRepository;
import com.badminton.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourtRepository courtRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_shouldCreatePendingBooking_whenValidRequest() {
        LocalDate bookingDate = LocalDate.now().plusDays(3);
        BookingCreateRequest request = BookingCreateRequest.builder()
                .courtId(1L)
                .timeSlotId(1L)
                .bookingDate(bookingDate)
                .build();

        User user = buildUser();
        Court court = buildCourt();
        TimeSlot timeSlot = buildTimeSlot();
        Booking savedBooking = Booking.builder().id(1L).status(BookingStatus.PENDING).build();
        BookingResponse response = BookingResponse.builder().id(1L).status(BookingStatus.PENDING).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(timeSlot));
        when(bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotId(1L, bookingDate, 1L)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingMapper.toResponse(savedBooking)).thenReturn(response);

        BookingResponse result = bookingService.createBooking(request, 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_shouldThrowConflict_whenDuplicateBooking() {
        LocalDate bookingDate = LocalDate.now().plusDays(3);
        BookingCreateRequest request = BookingCreateRequest.builder()
                .courtId(1L)
                .timeSlotId(1L)
                .bookingDate(bookingDate)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser()));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(buildCourt()));
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(buildTimeSlot()));
        when(bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotId(1L, bookingDate, 1L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(request, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Booking conflict");
    }

    @Test
    void createBooking_shouldThrowValidation_whenDateNotFuture() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .courtId(1L)
                .timeSlotId(1L)
                .bookingDate(LocalDate.now())
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(request, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("future");
    }

    @Test
    void approveBooking_shouldSetConfirmed_whenPending() {
        Booking booking = Booking.builder().id(1L).status(BookingStatus.PENDING).build();
        BookingResponse response = BookingResponse.builder().id(1L).status(BookingStatus.CONFIRMED).build();

        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(response);

        BookingResponse result = bookingService.approveBooking(1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    private User buildUser() {
        Role role = Role.builder().name(RoleType.ROLE_CUSTOMER).build();
        return User.builder()
                .id(1L)
                .email("customer@mail.com")
                .fullName("Customer")
                .enabled(true)
                .roles(Set.of(role))
                .build();
    }

    private Court buildCourt() {
        return Court.builder()
                .id(1L)
                .name("Court A")
                .pricePerHour(new BigDecimal("150000"))
                .active(true)
                .build();
    }

    private TimeSlot buildTimeSlot() {
        return TimeSlot.builder()
                .id(1L)
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(7, 0))
                .active(true)
                .build();
    }
}
