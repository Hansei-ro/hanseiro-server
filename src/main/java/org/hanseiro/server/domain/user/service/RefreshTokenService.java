package org.hanseiro.server.domain.user.service;

import org.hanseiro.server.domain.user.exception.AuthDomainException;
import org.hanseiro.server.domain.user.model.RefreshTokenEntity;
import org.hanseiro.server.domain.user.repository.RefreshTokenRepository;
import org.hanseiro.server.global.security.JwtProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 해시만 저장
    @Transactional
    public void store(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);
        Instant exp = jwtProvider.parse(refreshToken).getBody().getExpiration().toInstant();
        repo.save(RefreshTokenEntity.builder()
                .userId(userId)
                .tokenHash(sha256(refreshToken))
                .expiresAt(exp)
                .build());
    }

    @Transactional(readOnly = true)
    public Long validateAndGetUserId(String refreshToken) {
        try {
            if (!"refresh".equals(jwtProvider.getType(refreshToken))) {
                throw AuthDomainException.invalidRefreshToken();
            }

            String hash = sha256(refreshToken);
            RefreshTokenEntity entity = repo.findByTokenHash(hash)
                    .orElseThrow(AuthDomainException::invalidRefreshToken);

            if (entity.isRevoked()) throw AuthDomainException.invalidRefreshToken();
            if (entity.getExpiresAt().isBefore(Instant.now())) throw AuthDomainException.invalidRefreshToken();

            // 서명/만료는 jwtProvider.parse에서 이미 검증됨
            return entity.getUserId();

        } catch (AuthDomainException e) {
            throw e;
        } catch (Exception e) {
            // 예기치 못한 예외도 refresh 실패로 통일
            throw AuthDomainException.invalidRefreshToken();
        }
    }

    @Transactional
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
