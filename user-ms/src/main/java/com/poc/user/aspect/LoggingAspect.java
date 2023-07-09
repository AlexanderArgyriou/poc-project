package com.poc.user.aspect;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging execution of service, DAO, 
 * repository and controller methods and exceptions
 *
 * @author didel
 *
 */
@Aspect
@Component
@Log4j2
public class LoggingAspect {
    @Before("execution(* com.poc.user.service.*.*(..))")
    public void interceptMethodsBeforeCall(JoinPoint joinPoint) {
        log.info("Intercepted method: {}, called", () -> joinPoint.getSignature().toShortString());
    }

    @After("execution(* com.poc.user.service.*.*(..))")
    public void interceptMethodsAfterCall(JoinPoint joinPoint) {
        log.info("Intercepted method: {} end", () -> joinPoint.getSignature().toShortString());
    }

    @AfterThrowing(pointcut = "execution(* com.poc.user.service.*.*(..))", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with cause = {}",
                () -> joinPoint.getSignature().getDeclaringTypeName(),
                () -> joinPoint.getSignature().getName(),
                () -> e.getCause() != null ? e.getCause() : "NULL");
    }
}