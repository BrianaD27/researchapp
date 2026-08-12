package com.vsu.researchapp.infrastructure.security;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
        LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Wrong username or password
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {
        logger.warn("[SECURITY] Failed login attempt");
        return error(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    // Trying to access something without permission
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {
        logger.warn("[SECURITY] Access denied: {}", ex.getMessage());
        return error(HttpStatus.FORBIDDEN, "Access denied");
    }

    // DTO validation failed - @Valid on request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        logger.warn("[VALIDATION] {}", errors);
        return error(HttpStatus.BAD_REQUEST, errors);
    }

    // @Validated on controller params failed
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(
            ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations()
            .stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining(", "));
        logger.warn("[VALIDATION] {}", errors);
        return error(HttpStatus.BAD_REQUEST, errors);
    }

    // Missing required request parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex) {
        return error(HttpStatus.BAD_REQUEST,
            "Missing required parameter: " + ex.getParameterName());
    }

    // File too large
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileTooLarge(
            MaxUploadSizeExceededException ex) {
        return error(HttpStatus.BAD_REQUEST,
            "File size exceeds maximum allowed size of 10MB");
    }

    // Business logic errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(
            RuntimeException ex) {
        logger.error("[ERROR] {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Catch everything else - never expose stack traces
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(
            Exception ex) {
        logger.error("[UNHANDLED ERROR] {}", ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
            "error", message,
            "status", status.value(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
