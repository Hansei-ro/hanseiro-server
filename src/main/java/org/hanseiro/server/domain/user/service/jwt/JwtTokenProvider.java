package org.hanseiro.server.domain.user.service.jwt;

import java.time.LocalDateTime;

public interface JwtTokenProvider {

    String createAccessToken(Long userId, String email);

    String createRefreshToken(Long userId, String email);

    void validateAccessToken(String accessToken);

    void validateRefreshToken(String refreshToken);

    Long getUserIdFromAccessToken(String accessToken);

    LocalDateTime getRefreshTokenExpiry(String refreshToken);
}
