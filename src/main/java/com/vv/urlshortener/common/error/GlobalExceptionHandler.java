package com.vv.urlshortener.common.error;

import java.time.Instant;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.vv.urlshortener.shortlink.domain.AliasAlreadyExistsException;
import com.vv.urlshortener.shortlink.domain.InvalidUrlException;
import com.vv.urlshortener.shortlink.domain.InvalidExpirationException;
import com.vv.urlshortener.shortlink.domain.ShortLinkExpiredException;
import com.vv.urlshortener.shortlink.domain.ShortLinkNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiError> onValidationError(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ApiError error = new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", msg, req.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<ApiError> onConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream().map(v -> v.getMessage()).collect(Collectors.joining(", "));
        ApiError error = new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", msg, req.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidUrlException.class)
    @ResponseBody
    public ResponseEntity<ApiError> onInvalidUrl(InvalidUrlException ex, HttpServletRequest req) {
        ApiError error = new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "INVALID_URL", ex.getMessage(), req.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AliasAlreadyExistsException.class)
    @ResponseBody
    public ResponseEntity<ApiError> onAliasConflict(AliasAlreadyExistsException ex, HttpServletRequest req) {
        ApiError error = new ApiError(Instant.now(), HttpStatus.CONFLICT.value(), "ALIAS_CONFLICT", ex.getMessage(), req.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ShortLinkNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiError> onNotFound(ShortLinkNotFoundException ex, HttpServletRequest req) {
        ApiError error = new ApiError(Instant.now(), HttpStatus.NOT_FOUND.value(), "NOT_FOUND", ex.getMessage(), req.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ShortLinkExpiredException.class)
    @ResponseBody
    public ResponseEntity<ApiError> onExpired(ShortLinkExpiredException ex, HttpServletRequest req) {
        ApiError error = new ApiError(Instant.now(), HttpStatus.GONE.value(), "EXPIRED", ex.getMessage(), req.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ExceptionHandler(InvalidExpirationException.class)
    @ResponseBody
    public ResponseEntity<ApiError> onInvalidExpiration(InvalidExpirationException ex, HttpServletRequest req) {
        ApiError error = new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "INVALID_EXPIRATION", ex.getMessage(), req.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<ApiError> onDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ApiError error = new ApiError(Instant.now(), HttpStatus.CONFLICT.value(), "DATA_INTEGRITY", "Constraint violation", req.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
