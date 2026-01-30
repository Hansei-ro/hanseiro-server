package org.hanseiro.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.hanseiro.server.domain.user.exception.AuthDomainException;
import org.hanseiro.server.global.security.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenIssuer {
    public static final String REFRESH_HEADER = "X-Refresh-Token";

    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public void issue(Long userId, HttpHeaders responseHeaders) {
        try {
            String access = jwtProvider.createAccessToken(userId);
            String refresh = jwtProvider.createRefreshToken(userId);

            refreshTokenService.store(refresh);

            responseHeaders.set(REFRESH_HEADER, refresh);
            responseHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + access);
        } catch (Exception e) {
            // JWT 생성 또는 저장 중 오류
            throw AuthDomainException.tokenIssueFailed();
        }
    }

    public void issueAccessOnly(Long userId, HttpHeaders responseHeaders) {
        try {
            String access = jwtProvider.createAccessToken(userId);
            responseHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + access);
        } catch (Exception e) {
            // JWT 생성 중 오류
            throw AuthDomainException.tokenIssueFailed();
        }
    }
}
