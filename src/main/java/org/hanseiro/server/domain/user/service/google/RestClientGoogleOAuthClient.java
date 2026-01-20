package org.hanseiro.server.domain.user.service.google;


import org.hanseiro.server.domain.user.service.google.dto.GoogleTokenResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientGoogleOAuthClient implements GoogleOAuthClient{

    private final RestClient restClient;
    private final GoogleOAuthProperties props;

    public RestClientGoogleOAuthClient(RestClient.Builder builder, GoogleOAuthProperties props) {
        this.restClient = builder.build();
        this.props = props;
    }

    public GoogleTokenResponse exchangeCodeForToken(String code, String redirectUri) {
        // token endpoint는 x-www-form-urlencoded 권장
        return restClient.post()
                .uri(props.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("code=" + encode(code)
                        + "&client_id=" + encode(props.getClientId())
                        + "&client_secret=" + encode(props.getClientSecret())
                        + "&redirect_uri=" + encode(redirectUri)
                        + "&grant_type=authorization_code")
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    public GoogleUserInfo fetchUserInfo(String googleAccessToken) {
        return restClient.get()
                .uri(props.getUserinfoUri())
                .header("Authorization", "Bearer " + googleAccessToken)
                .retrieve()
                .body(GoogleUserInfo.class);
    }

    private String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}

