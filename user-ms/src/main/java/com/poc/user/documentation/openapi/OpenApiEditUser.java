package com.poc.user.documentation.openapi;

import com.poc.user.domain.response.ApiErrorResponse;
import com.poc.user.domain.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Operation(summary = "Edit a user (if used not exists it gets created with id specified)")
@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "User created",
        content = {@Content(mediaType = "application/json",schema =@Schema(implementation = UserResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid user data",
                content = {@Content(mediaType = "application/json",schema =@Schema(implementation = ApiErrorResponse.class))})})
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OpenApiEditUser {
}