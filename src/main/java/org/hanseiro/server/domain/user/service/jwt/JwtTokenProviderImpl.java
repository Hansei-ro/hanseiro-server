package org.hanseiro.server.domain.user.service.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProviderImpl implements JwtTokenProvider{
    private final JwtProperties props;
    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // 토큰생성
    @Override
    public String createAccessToken(Long userId, String email) {
        return createToken(
                userId,
                email,
                props.getAccessTokenExpireSeconds(),
                TokenType.ACCESS
        );
    }

    @Override
    public String createRefreshToken(Long userId, String email) {
        return createToken(
                userId,
                email,
                props.getRefreshTokenExpireSeconds(),
                TokenType.REFRESH
        );
    }

    private String createToken(Long userId, String email, long expireSeconds, TokenType type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireSeconds * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("type", type.name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 코큰검증
    @Override
    public void validateAccessToken(String accessToken) {
        validate(accessToken, TokenType.ACCESS);
    }

    @Override
    public void validateRefreshToken(String refreshToken) {
        validate(refreshToken, TokenType.REFRESH);
    }

    private void validate(String token, TokenType expectedType) {
        try {
            Claims claims = parseClaims(token);

            String type = claims.get("type", String.class);
            if (!expectedType.name().equals(type)) {
                throw new SecurityException("잘못된 토큰 타입입니다.");
            }

        } catch (ExpiredJwtException e) {
            throw new SecurityException("토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new SecurityException("유효하지 않은 토큰입니다.");
        }
    }

    // 유저정보추출
    @Override
    public Long getUserIdFromAccessToken(String accessToken) {
        Claims claims = parseClaims(accessToken);
        return Long.valueOf(claims.getSubject());
    }

    @Override
    public LocalDateTime getRefreshTokenExpiry(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        Date expiration = claims.getExpiration();
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private enum TokenType {
        ACCESS, REFRESH
    }
}
