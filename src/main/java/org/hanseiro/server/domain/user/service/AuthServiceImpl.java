package org.hanseiro.server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.hanseiro.server.domain.user.service.dto.UserResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleLoginRequest;
import org.hanseiro.server.domain.user.model.UserEntity;
import org.hanseiro.server.domain.user.repository.UserRepository;
import org.hanseiro.server.domain.user.service.google.GoogleUserUpsertService;
import org.hanseiro.server.domain.user.service.google.RestClientGoogleOAuthClient;
import org.hanseiro.server.domain.user.service.google.dto.GoogleTokenResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;
import org.hanseiro.server.domain.user.validator.HanseiEmailValidator;
import org.hanseiro.server.domain.user.validator.UserNameParser;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final RestClientGoogleOAuthClient googleService;
    private final HanseiEmailValidator accountValidator;
    private final UserNameParser userNameParser;
    private final GoogleUserUpsertService userUpsertService;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public UserResponse loginWithGoogle(GoogleLoginRequest req, HttpHeaders responseHeaders) {
        // code로 google token교환
        GoogleTokenResponse token = googleService.exchangeCodeForToken(req.authorizationCode(), req.redirectUri());

        // google token으로 userinfo조회
        GoogleUserInfo userInfo = googleService.fetchUserInfo(token.accessToken());

        // 한세대 이메일/verified 검증
        String email = accountValidator.validateAndGetEmail(userInfo);

        // 이름/학과 파싱
        UserNameParser.ParsedUserName parsed = userNameParser.parse(userInfo.name());

        // 유저 upsert + 프로필 반영
        GoogleUserUpsertService.UpsertResult result = userUpsertService.upsert(email, parsed);
        UserEntity user = result.user();
        boolean isNew = result.isNew();

        // 헤더에 access/refresh 세팅
        tokenIssuer.issue(user.getId(), responseHeaders);

        // 바디에 유저 정보 반환
        return UserResponse.of(user, isNew);
    }

    @Override
    @Transactional(readOnly = true)
    public void refresh(String refreshToken, HttpHeaders responseHeaders) {
        Long userId = refreshTokenService.validateAndGetUserId(refreshToken);
        // access 재발급
        tokenIssuer.issueAccessOnly(userId, responseHeaders);

    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
}
