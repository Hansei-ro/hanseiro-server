package org.hanseiro.server.domain.user.controller;

import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private CookieUtil() {}

    public static ResponseCookie refreshTokenCookie(String refreshToken, boolean secure, String sameSite, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)             //http면 false, https면 true
                .path("/api/v1/auth")
                .sameSite(sameSite)
                .maxAge(maxAgeSeconds)
                .build();
    }

    public static ResponseCookie deleteRefreshTokenCookie(boolean secure, String sameSite) {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .path("/api/v1/auth")
                .sameSite(sameSite)
                .maxAge(0)
                .build();
    }
}
