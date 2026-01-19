package org.hanseiro.server.domain.user.service.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("access_token") String accessToken,
        // access token 만료시간
        @JsonProperty("expires_in") Long expiresIn,
        // 토큰 타입
        @JsonProperty("token_type") String tokenType,
        // 승인 된 스코프
        @JsonProperty("scope") String scope,
        @JsonProperty("id_token") String idToken
) {}
