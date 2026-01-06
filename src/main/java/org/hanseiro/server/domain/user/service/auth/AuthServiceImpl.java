package org.hanseiro.server.domain.user.service.auth;

import lombok.RequiredArgsConstructor;
import org.hanseiro.server.domain.user.model.entity.OAuthAccountEntity;
import org.hanseiro.server.domain.user.model.entity.RefreshTokenEntity;
import org.hanseiro.server.domain.user.model.SocialProvider;
import org.hanseiro.server.domain.user.model.entity.UserEntity;
import org.hanseiro.server.domain.user.repository.OAuthAccountRepository;
import org.hanseiro.server.domain.user.repository.RefreshTokenRepository;
import org.hanseiro.server.domain.user.repository.UserRepository;
import org.hanseiro.server.domain.user.service.google.GoogleOAuthService;
import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;
import org.hanseiro.server.domain.user.service.auth.dto.AuthTokenPair;
import org.hanseiro.server.domain.user.service.jwt.JwtTokenProvider;
import org.hanseiro.server.domain.user.service.token.RefreshTokenHasher;
import org.hanseiro.server.domain.user.validator.GoogleUserValidator;
import org.hanseiro.server.domain.user.validator.parser.UserNameParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final GoogleOAuthService googleOAuthService;
    private final GoogleUserValidator googleUserValidator;
    private final UserNameParser userNameParser;

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;

    // 인가코드 기반 로그인(구글콜백처리)
    @Transactional
    public AuthTokenPair loginWithGoogleCode(String code) {
        GoogleUserInfo info = googleOAuthService.fetchUserInfoByCode(code);

        googleUserValidator.validate(info);

        UserNameParser.ParsedUserName parsed = userNameParser.parse(info.getName());

        // OAuthAccount로 유저 찾기
        OAuthAccountEntity account = oAuthAccountRepository
                .findByProviderAndProviderSubject(SocialProvider.GOOGLE, info.getId())
                .orElse(null);

        UserEntity user;
        if (account != null) {
            // 기존 유저
            user = account.getUser();
            user.updateProfile(parsed.department(), parsed.name());
            userRepository.save(user);
        } else {
            // 신규 유저
            user = UserEntity.builder()
                    .email(info.getEmail())
                    .department(parsed.department())
                    .name(parsed.name())
                    .build();
            user = userRepository.save(user);

            OAuthAccountEntity newAccount = OAuthAccountEntity.builder()
                    .provider(SocialProvider.GOOGLE)
                    .providerSubject(info.getId())
                    .user(user)
                    .build();
            oAuthAccountRepository.save(newAccount);
        }

        // 한세로 JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        // refresh토큰 해시 저장
        storeRefreshToken(user, refreshToken);

        return new AuthTokenPair(accessToken, refreshToken);
    }

    @Transactional
    public AuthTokenPair refresh(String rawRefreshToken) {
        jwtTokenProvider.validateRefreshToken(rawRefreshToken);

        String hash = refreshTokenHasher.hash(rawRefreshToken);

        RefreshTokenEntity saved = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new SecurityException("유효하지 않은 refresh token 입니다."));

        if (saved.isExpired()) {
            throw new SecurityException("만료된 refresh token 입니다.");
        }

        UserEntity user = saved.getUser();

        refreshTokenRepository.delete(saved);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        storeRefreshToken(user, newRefreshToken);

        return new AuthTokenPair(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteAllByUser_Id(userId);
    }

    private void storeRefreshToken(UserEntity user, String rawRefreshToken) {
        String hash = refreshTokenHasher.hash(rawRefreshToken);

        LocalDateTime expiresAt = jwtTokenProvider.getRefreshTokenExpiry(rawRefreshToken);

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .tokenHash(hash)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(entity);
    }
}
