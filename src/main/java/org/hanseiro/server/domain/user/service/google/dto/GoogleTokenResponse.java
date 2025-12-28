package org.hanseiro.server.domain.user.service.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    // access token 만료시간
    @JsonProperty("expires_in")
    private Long expiresIn;

    // 토큰 타입
    @JsonProperty("token_type")
    private String tokenType;

    // 승인 된 스코프
    private String scope;

}
