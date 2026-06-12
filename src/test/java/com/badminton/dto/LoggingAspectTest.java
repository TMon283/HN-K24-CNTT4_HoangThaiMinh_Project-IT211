package com.badminton.dto;

import com.badminton.aop.LoggingAspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private JoinPoint joinPoint;
    @Mock
    private Signature signature;

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Test
    void logBefore_shouldNotThrowException() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.badminton.service.impl.AuthServiceImpl");
        when(signature.getName()).thenReturn("login");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"customer@mail.com"});

        loggingAspect.logBefore(joinPoint);
    }

    @Test
    void logAfterThrowing_shouldNotThrowException() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.badminton.service.impl.BookingServiceImpl");
        when(signature.getName()).thenReturn("createBooking");

        loggingAspect.logAfterThrowing(joinPoint, new RuntimeException("Booking conflict"));
    }
}
