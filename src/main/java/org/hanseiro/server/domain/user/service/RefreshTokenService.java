package org.hanseiro.server.domain.user.service;

import org.hanseiro.server.domain.user.model.RefreshTokenEntity;
import org.hanseiro.server.domain.user.repository.RefreshTokenRepository;
import org.hanseiro.server.global.security.JwtProvider;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final JwtProvider jwtProvider;

    public RefreshTokenService(RefreshTokenRepository repo, JwtProvider jwtProvider) {
        this.repo = repo;
        this.jwtProvider = jwtProvider;
    }

    // 저장: 해시만 저장
    public void store(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);
        Instant exp = jwtProvider.parse(refreshToken).getBody().getExpiration().toInstant();
        repo.save(new RefreshTokenEntity(userId, sha256(refreshToken), exp));
    }

    public Long validateAndGetUserId(String refreshToken) {
        if (!"refresh".equals(jwtProvider.getType(refreshToken))) {
            throw new IllegalArgumentException("Refresh token type mismatch");
        }

        String hash = sha256(refreshToken);
        RefreshTokenEntity entity = repo.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (entity.isRevoked()) throw new IllegalArgumentException("Refresh token revoked");
        if (entity.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Refresh token expired");

        // 서명/만료는 jwtProvider.parse에서 이미 검증됨
        return entity.getUserId();
    }

    public void revoke(String refreshToken) {
        String hash = sha256(refreshToken);
        repo.findByTokenHash(hash).ifPresent(e -> {
            e.revoke();
            repo.save(e);
        });
    }

    private String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
