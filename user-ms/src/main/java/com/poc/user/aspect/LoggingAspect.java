package com.poc.user.aspect;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging execution of service, DAO,
 * repository and controller methods and exceptions
 *
 * @author didel
 */
@Aspect
@Component
@Log4j2
public class LoggingAspect {
    @Around("execution(* com.poc.user.service.*.*(..))")
    @SneakyThrows
    public Object interceptAroundMethods(ProceedingJoinPoint joinPoint) {
        log.info("Intercepted method: {}, called", () -> joinPoint.getSignature().toShortString());
        Object proceed = joinPoint.proceed();
        log.info("Intercepted method: {} end", () -> joinPoint.getSignature().toShortString());
        return proceed;
    }
}