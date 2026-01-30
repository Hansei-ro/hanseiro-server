package org.hanseiro.server.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseiro.server.domain.user.service.dto.UserResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleLoginRequest;
import org.hanseiro.server.domain.user.service.AuthService;
import org.hanseiro.server.domain.user.service.TokenIssuer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private static final String REFRESH_HEADER = TokenIssuer.REFRESH_HEADER;

    @PostMapping("/google")
    public ResponseEntity<UserResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest req) {
        // System.out.println("HIT /auth/google : " + req);
        HttpHeaders headers = new HttpHeaders();
        UserResponse userResponse = authService.loginWithGoogle(req, headers);
        return ResponseEntity.ok().headers(headers).body(userResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@RequestHeader(REFRESH_HEADER) String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        authService.refresh(refreshToken, headers);
        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(REFRESH_HEADER) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

}
