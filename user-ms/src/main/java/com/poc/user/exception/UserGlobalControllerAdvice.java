package com.poc.user.exception;

import com.poc.user.domain.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class UserGlobalControllerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleUserNotFoundException(UserNotFoundException exception, ServerWebExchange exchange) {
        ApiErrorResponse apiErrorResponse = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .path(exchange.getRequest().getPath().value())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(exception.getMessage())
                .build();
        return Mono.just(new ResponseEntity<>(apiErrorResponse, HttpStatus.NOT_FOUND));
    }
}
