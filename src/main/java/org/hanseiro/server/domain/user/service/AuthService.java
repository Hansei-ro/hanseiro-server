package org.hanseiro.server.domain.user.service;

import org.hanseiro.server.domain.user.dto.GoogleLoginRequest;
import org.hanseiro.server.domain.user.dto.TokenResponse;
import org.springframework.http.HttpHeaders;

public interface AuthService {
    TokenResponse loginWithGoogle(GoogleLoginRequest req, HttpHeaders responseHeaders);
    TokenResponse refresh(String refreshToken, HttpHeaders responseHeaders);
    void logout(String refreshToken);
}
