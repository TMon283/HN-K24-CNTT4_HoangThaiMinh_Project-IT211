package com.badminton.controller;

import com.badminton.constant.BookingStatus;
import com.badminton.dto.response.BookingResponse;
import com.badminton.dto.response.CourtImageResponse;
import com.badminton.service.BookingService;
import com.badminton.service.CourtService;
import com.badminton.support.MockMvcTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;
    @Mock
    private CourtService courtService;

    @InjectMocks
    private ManagerController managerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcTestSupport.buildMockMvc(managerController);
    }

    @Test
    void getCourtImages_shouldReturn200() throws Exception {
        when(courtService.getCourtImages(1L))
                .thenReturn(List.of(CourtImageResponse.builder()
                        .id(1L)
                        .courtId(1L)
                        .imageUrl("https://example.com/1.jpg")
                        .build()));

        mockMvc.perform(get("/api/v1/manager/courts/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].imageUrl").value("https://example.com/1.jpg"));
    }

    @Test
    void approveBooking_shouldReturnConfirmedStatus() throws Exception {
        when(bookingService.approveBooking(1L))
                .thenReturn(BookingResponse.builder().id(1L).status(BookingStatus.CONFIRMED).build());

        mockMvc.perform(put("/api/v1/manager/bookings/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void rejectBooking_shouldReturnCancelledStatus() throws Exception {
        when(bookingService.rejectBooking(2L))
                .thenReturn(BookingResponse.builder().id(2L).status(BookingStatus.CANCELLED).build());

        mockMvc.perform(put("/api/v1/manager/bookings/2/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
