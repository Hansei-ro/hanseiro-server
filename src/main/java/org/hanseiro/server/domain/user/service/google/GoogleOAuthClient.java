package org.hanseiro.server.domain.user.service.google;

import org.hanseiro.server.domain.user.service.google.dto.GoogleTokenResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;

public interface GoogleOAuthClient {
    GoogleTokenResponse exchangeCodeForToken(String code, String redirectUri);
    GoogleUserInfo fetchUserInfo(String googleAccessToken);
}
