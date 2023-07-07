package com.poc.user.domain.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiErrorResponse {
    private Instant timestamp;
    private String path;
    private int status;
    private String error;
    private String message;
}
