package org.hanseiro.server.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hanseiro.server.domain.user.controller.dto.AccessTokenResponse;
import org.hanseiro.server.domain.user.service.auth.AuthService;
import org.hanseiro.server.domain.user.service.auth.dto.AuthTokenPair;
import org.hanseiro.server.domain.user.service.google.GoogleOAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {
    private final GoogleOAuthService googleAuthService;
    private final AuthService authService;

    private static final boolean COOKIE_SECURE = false;      //http면 false, https면 true
    private static final String COOKIE_SAMESITE = "Lax";
    private static final long REFRESH_COOKIE_MAX_AGE_SECONDS = 60L * 60 * 24 * 14;

    @GetMapping("/google/authorize")
    public void googleAuthorize(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        String url = googleAuthService.buildAuthorizeUrl(state);
        response.sendRedirect(url);
    }

    @GetMapping("/google/callback")
    public ResponseEntity<AccessTokenResponse> googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state
    ) {
        return okWithRefreshCookie(authService.loginWithGoogleCode(code));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new SecurityException("refresh token 쿠키가 없습니다.");
        }
        return okWithRefreshCookie(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("인증 정보가 없습니다.");
        }

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.logout(userId);

        ResponseCookie deleteCookie = CookieUtil.deleteRefreshTokenCookie(COOKIE_SECURE, COOKIE_SAMESITE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    // access응답 + refresh쿠키 세팅
    private ResponseEntity<AccessTokenResponse> okWithRefreshCookie(AuthTokenPair pair) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(pair.refreshToken()).toString())
                .body(new AccessTokenResponse(pair.accessToken()));
    }

    // refresh쿠키 생성
    private ResponseCookie refreshCookie(String refreshToken) {
        return CookieUtil.refreshTokenCookie(
                refreshToken,
                COOKIE_SECURE,
                COOKIE_SAMESITE,
                REFRESH_COOKIE_MAX_AGE_SECONDS
        );
    }
}
