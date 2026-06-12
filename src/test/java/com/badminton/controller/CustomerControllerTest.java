package com.badminton.controller;

import com.badminton.constant.BookingStatus;
import com.badminton.constant.RoleType;
import com.badminton.dto.request.BookingCreateRequest;
import com.badminton.dto.response.BookingResponse;
import com.badminton.entity.Role;
import com.badminton.entity.User;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.security.UserPrincipal;
import com.badminton.service.BookingService;
import com.badminton.support.MockMvcTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().name(RoleType.ROLE_CUSTOMER).build();
        User user = User.builder()
                .id(1L)
                .email("customer@mail.com")
                .password("encoded")
                .fullName("Customer")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcTestSupport.buildMockMvc(customerController);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBooking_shouldReturn201_whenValidRequest() throws Exception {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .courtId(1L)
                .timeSlotId(1L)
                .bookingDate(LocalDate.now().plusDays(5))
                .build();

        when(bookingService.createBooking(any(BookingCreateRequest.class), eq(1L)))
                .thenReturn(BookingResponse.builder()
                        .id(1L)
                        .status(BookingStatus.PENDING)
                        .courtId(1L)
                        .build());

        mockMvc.perform(post("/api/v1/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void createBooking_shouldReturn400_whenDateNotFuture() throws Exception {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .courtId(1L)
                .timeSlotId(1L)
                .bookingDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
