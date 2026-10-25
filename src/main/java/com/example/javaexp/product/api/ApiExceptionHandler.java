package com.example.javaexp.product.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.javaexp.product.service.InvalidSearchCriteriaException;
import com.example.javaexp.product.service.ProductNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ProductNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        Instant.now(),
                        HttpStatus.NOT_FOUND.value(),
                        exception.getMessage(),
                        Map.of()));
    }

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    ResponseEntity<ApiError> handleInvalidSearchCriteria(
            InvalidSearchCriteriaException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage(),
                        Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(new ApiError(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Request validation failed",
                        fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleParameterValidation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage(),
                        Map.of()));
    }

    record ApiError(
            Instant timestamp,
            int status,
            String message,
            Map<String, String> fieldErrors) {
    }
}
