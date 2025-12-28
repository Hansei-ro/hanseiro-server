package org.hanseiro.server.domain.user.service.google;

import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;

public interface GoogleOAuthService {
    String buildAuthorizeUrl(String state);
    GoogleUserInfo fetchUserInfoByCode(String code);
}
