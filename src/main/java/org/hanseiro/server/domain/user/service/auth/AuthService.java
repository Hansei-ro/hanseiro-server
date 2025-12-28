package org.hanseiro.server.domain.user.service.auth;

import org.hanseiro.server.domain.user.service.auth.dto.AuthTokenPair;

public interface AuthService {
    AuthTokenPair loginWithGoogleCode(String code);

    AuthTokenPair refresh(String refreshToken);

    void logout(Long userId);
}
