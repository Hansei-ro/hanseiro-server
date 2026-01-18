package org.hanseiro.server.domain.user.service;

import org.hanseiro.server.domain.user.dto.GoogleLoginRequest;
import org.hanseiro.server.domain.user.dto.TokenResponse;
import org.hanseiro.server.domain.user.model.UserEntity;
import org.hanseiro.server.domain.user.repository.UserRepository;
import org.hanseiro.server.domain.user.service.google.RestClientGoogleOAuthClient;
import org.hanseiro.server.domain.user.service.google.dto.GoogleTokenResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;
import org.hanseiro.server.global.security.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    public static final String REFRESH_HEADER = "X-Refresh-Token";

    private final RestClientGoogleOAuthClient googleService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenServiceImpl refreshTokenService;

    public AuthServiceImpl(RestClientGoogleOAuthClient googleService,
                           UserRepository userRepository,
                           JwtProvider jwtProvider,
                           RefreshTokenServiceImpl refreshTokenService) {
        this.googleService = googleService;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public TokenResponse loginWithGoogle(GoogleLoginRequest req, HttpHeaders responseHeaders) {
        GoogleTokenResponse token = googleService.exchangeCodeForToken(req.authorizationCode(), req.redirectUri());
        GoogleUserInfo userInfo = googleService.fetchUserInfo(token.accessToken());

        // 1) email_verified 확인
        if (userInfo.emailVerified() == null || !userInfo.emailVerified()) {
            throw new IllegalArgumentException("Google email is not verified");
        }

        // 2) 도메인 체크
        String email = userInfo.email();
        if (email == null || !email.endsWith("@hansei.ac.kr")) {
            throw new IllegalArgumentException("Not a Hansei account");
        }

        // 3) 유저 조회/생성
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(UserEntity.create(email, userInfo.name())));

        // 4) 한세로 JWT 발급
        String access = jwtProvider.createAccessToken(user.getId());
        String refresh = jwtProvider.createRefreshToken(user.getId());

        // 5) refresh 저장
        refreshTokenService.store(refresh);

        // 6) refresh는 헤더로 전달
        responseHeaders.add(REFRESH_HEADER, refresh);

        return new TokenResponse(access, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken, HttpHeaders responseHeaders) {
        Long userId = refreshTokenService.validateAndGetUserId(refreshToken);

        // access 재발급
        String newAccess = jwtProvider.createAccessToken(userId);

        return new TokenResponse(newAccess, userId);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // refresh 폐기
        refreshTokenService.revoke(refreshToken);
    }
}
