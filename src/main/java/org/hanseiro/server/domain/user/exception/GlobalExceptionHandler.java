package org.hanseiro.server.domain.user.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;

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
}
