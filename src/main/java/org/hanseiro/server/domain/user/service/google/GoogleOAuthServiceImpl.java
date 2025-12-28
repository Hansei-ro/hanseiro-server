package org.hanseiro.server.domain.user.service.google;

import org.hanseiro.server.domain.user.exception.SocialLoginException;
import org.hanseiro.server.domain.user.service.google.dto.GoogleTokenResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private final GoogleOAuthProperties props;
    private final RestTemplate restTemplate;

    // 구글 로그인 페이지 URL 생성
    @Override
    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", props.getClientId())
                .queryParam("redirect_uri", props.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", props.getScope())
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }

    // 인가 코드로 토큰 발급 후 userinfo 조회
    @Override
    public GoogleUserInfo fetchUserInfoByCode(String code) {
        GoogleTokenResponse tokenResponse = exchangeCodeForToken(code);

        if (tokenResponse == null || isBlank(tokenResponse.getAccessToken())) {
            throw new SocialLoginException("GOOGLE_TOKEN_EXCHANGE_FAILED", "구글 토큰 발급에 실패했습니다.");
        }

        return fetchUserInfo(tokenResponse.getAccessToken());
    }

    // 인가 코드 -> Access Token
    private GoogleTokenResponse exchangeCodeForToken(String code) {
        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", props.getClientId());
        body.add("client_secret", props.getClientSecret());
        body.add("redirect_uri", props.getRedirectUri());
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GoogleTokenResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, GoogleTokenResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SocialLoginException("GOOGLE_TOKEN_HTTP_ERROR", "구글 토큰 요청이 실패했습니다.");
            }
            return response.getBody();
        } catch (Exception e) {
            throw new SocialLoginException("GOOGLE_TOKEN_REQUEST_FAILED", "구글 토큰 요청 중 오류가 발생했습니다.");
        }
    }

    // 구글 UserInfo API 호출
    private GoogleUserInfo fetchUserInfo(String accessToken) {
        String url = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfo> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, GoogleUserInfo.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SocialLoginException("GOOGLE_USERINFO_HTTP_ERROR", "구글 사용자 정보 조회에 실패했습니다.");
            }
            return response.getBody();
        } catch (Exception e) {
            throw new SocialLoginException("GOOGLE_USERINFO_REQUEST_FAILED", "구글 사용자 정보 조회 중 오류가 발생했습니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
