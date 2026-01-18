package org.hanseiro.server.global.exception;

import org.hanseiro.server.domain.user.exception.InvalidSchoolEmailException;
import org.hanseiro.server.domain.user.exception.SocialLoginException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 학교 이메일이 아닌 계정으로 로그인 시도
    @ExceptionHandler(InvalidSchoolEmailException.class)
    public ResponseEntity<?> handleInvalidSchoolEmail(InvalidSchoolEmailException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", "INVALID_SCHOOL_EMAIL",
                "message", e.getMessage()
        ));
    }

    // 소셜 로그인 오류
    @ExceptionHandler(SocialLoginException.class)
    public ResponseEntity<?> handleSocialLogin(SocialLoginException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", e.getCode(),
                "message", e.getMessage()
        ));
    }

    // 인증은 ok, 권한이 없거나 토큰이 유효하지 않음
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurity(SecurityException e) {
        return ResponseEntity.status(401).body(Map.of(
                "code", "UNAUTHORIZED",
                "message", e.getMessage()
        ));
    }

    // 로그아웃 후 재리프레시 막기
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(401).body(Map.of(
                "code", "UNAUTHORIZED",
                "message", e.getMessage()
        ));
    }
}
