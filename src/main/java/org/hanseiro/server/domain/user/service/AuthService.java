package org.hanseiro.server.domain.user.service;

import org.hanseiro.server.domain.user.service.dto.UserResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleLoginRequest;
import org.springframework.http.HttpHeaders;

public interface AuthService {
    UserResponse loginWithGoogle(GoogleLoginRequest req, HttpHeaders responseHeaders);
    void refresh(String refreshToken, HttpHeaders responseHeaders);
    void logout(String refreshToken);
}
