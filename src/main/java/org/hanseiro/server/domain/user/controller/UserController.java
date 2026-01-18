package org.hanseiro.server.domain.user.controller;

import jakarta.validation.Valid;
import org.hanseiro.server.domain.user.dto.GoogleLoginRequest;
import org.hanseiro.server.domain.user.dto.TokenResponse;
import org.hanseiro.server.domain.user.service.AuthService;
import org.hanseiro.server.domain.user.service.AuthServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final AuthService authService;
    private static final String REFRESH_HEADER = AuthServiceImpl.REFRESH_HEADER;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> google(@RequestBody @Valid GoogleLoginRequest req) {
        // System.out.println("HIT /auth/google : " + req);
        HttpHeaders headers = new HttpHeaders();
        TokenResponse body = authService.loginWithGoogle(req, headers);
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader(REFRESH_HEADER) String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        TokenResponse body = authService.refresh(refreshToken, headers);
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(REFRESH_HEADER) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

}
