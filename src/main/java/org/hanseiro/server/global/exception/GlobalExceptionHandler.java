package org.hanseiro.server.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.hanseiro.server.domain.user.exception.AuthDomainException;
import org.springframework.http.ResponseEntity;
import org.hanseiro.server.global.dto.ErrorResponse;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> missingHeader(MissingRequestHeaderException e, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.of("MISSING_HEADER", e.getMessage())
        );
    }

    @ExceptionHandler(AuthDomainException.class)
    public ResponseEntity<ErrorResponse> handleAuthDomain(AuthDomainException e) {
        return ResponseEntity.status(e.getStatus()).body(
                ErrorResponse.of(e.getCode(), e.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.of("BAD_REQUEST", e.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unknown(Exception e, HttpServletRequest req) {
        return ResponseEntity.internalServerError().body(
                ErrorResponse.of("INTERNAL_ERROR", "Internal server error")
        );
    }
}
