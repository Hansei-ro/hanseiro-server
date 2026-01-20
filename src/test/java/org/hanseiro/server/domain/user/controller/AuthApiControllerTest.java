package org.hanseiro.server.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hanseiro.server.domain.user.dto.GoogleLoginRequest;
import org.hanseiro.server.domain.user.dto.TokenResponse;
import org.hanseiro.server.domain.user.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthApiControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;

    private static final String REFRESH_HEADER = "X-Refresh-Token";

    @Test
    @DisplayName("POST /api/v1/auth/google - 인가 코드로 로그인 성공")
    void googleLogin_success_and_sets_refresh_header() throws Exception {
        Mockito.when(authService.loginWithGoogle(any(GoogleLoginRequest.class), any(HttpHeaders.class)))
                .thenAnswer(inv -> {
                    HttpHeaders headers = inv.getArgument(1, HttpHeaders.class);
                    headers.add(REFRESH_HEADER, "test-refresh-token");
                    return new TokenResponse("test-access-token", 1L);
                });

        String body = objectMapper.writeValueAsString(
                new GoogleLoginRequest("test-authorization-code", "http://localhost:3000/oauth/callback")
        );

        mockMvc.perform(post("/api/v1/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string(REFRESH_HEADER, "test-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Refresh Token 헤더로 access token 재발급 성공")
    void refresh_success() throws Exception {
        Mockito.when(authService.refresh(anyString(), any(HttpHeaders.class)))
                .thenReturn(new TokenResponse("new-access-token", 1L));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .header(REFRESH_HEADER, "test-refresh-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Refresh Token 무효화 성공")
    void logout_success() throws Exception {
        Mockito.doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .header(REFRESH_HEADER, "test-refresh-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - refresh 헤더 누락 시 4xx")
    void refresh_missing_header_fail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().is4xxClientError());
    }
}
