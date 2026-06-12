package com.badminton.dto;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(com.badminton.controller..*) || within(com.badminton.service.impl..*)")
    public void applicationLayer() {
    }

    @Before("applicationLayer()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("[START] {}.{}() args={}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                formatArgs(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "applicationLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("[SUCCESS] {}.{}()",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "applicationLayer()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("[FAILED] {}.{}() - {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                exception.getMessage(),
                exception);
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) {
                        return "null";
                    }
                    String value = arg.toString();
                    if (value.toLowerCase().contains("password")) {
                        return "***";
                    }
                    return value.length() > 100 ? value.substring(0, 100) + "..." : value;
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
