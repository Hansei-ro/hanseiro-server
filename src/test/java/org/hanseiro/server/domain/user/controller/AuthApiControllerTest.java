package org.hanseiro.server.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNull;
import org.hanseiro.server.domain.user.service.dto.UserResponse;
import org.hanseiro.server.domain.user.service.google.dto.GoogleLoginRequest;
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

import static org.hamcrest.Matchers.startsWith;
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
    void googleLogin_success_and_returns_user_info() throws Exception {
        UserResponse mockUserResponse = new UserResponse(
                123L,
                "student@hansei.ac.kr",
                "이유진",
                "컴퓨터공학과",
                false
        );

        Mockito.when(authService.loginWithGoogle(any(GoogleLoginRequest.class), any(HttpHeaders.class)))
                .thenAnswer(inv -> {
                    HttpHeaders headers = inv.getArgument(1, HttpHeaders.class);
                    headers.add(REFRESH_HEADER, "test-refresh-token");
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer test-access-token");
                    return mockUserResponse;
                });

        String body = objectMapper.writeValueAsString(
                new GoogleLoginRequest("test-authorization-code", "http://localhost:3000/oauth/callback")
        );

        mockMvc.perform(post("/api/v1/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string(REFRESH_HEADER, "test-refresh-token"))
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer test-access-token"))
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.email").value("student@hansei.ac.kr"))
                .andExpect(jsonPath("$.name").value("이유진"))
                .andExpect(jsonPath("$.department").value("컴퓨터공학과"))
                .andExpect(jsonPath("$.isNew").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/google - 신규 가입자 로그인 성공")
    void googleLogin_new_user_success() throws Exception {
        UserResponse mockUserResponse = new UserResponse(
                456L,
                "newstudent@hansei.ac.kr",
                "김태남",
                "컴퓨터공학과",
                true  // 신규 가입자
        );

        Mockito.when(authService.loginWithGoogle(any(GoogleLoginRequest.class), any(HttpHeaders.class)))
                .thenAnswer(inv -> {
                    HttpHeaders headers = inv.getArgument(1, HttpHeaders.class);
                    headers.add(REFRESH_HEADER, "new-user-refresh-token");
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer new-user-access-token");
                    return mockUserResponse;
                });

        String body = objectMapper.writeValueAsString(
                new GoogleLoginRequest("new-user-code", "http://localhost:3000/oauth/callback")
        );

        mockMvc.perform(post("/api/v1/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string(REFRESH_HEADER, "new-user-refresh-token"))
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer new-user-access-token"))
                .andExpect(jsonPath("$.id").value(456))
                .andExpect(jsonPath("$.email").value("newstudent@hansei.ac.kr"))
                .andExpect(jsonPath("$.name").value("김태남"))
                .andExpect(jsonPath("$.department").value("컴퓨터공학과"))
                .andExpect(jsonPath("$.isNew").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/google - name, department null인 경우")
    void googleLogin_with_null_fields() throws Exception {
        UserResponse mockUserResponse = new UserResponse(
                789L,
                "nostudent@hansei.ac.kr",
                null,  // name이 null
                null,        // department가 null
                false
        );

        Mockito.when(authService.loginWithGoogle(any(GoogleLoginRequest.class), any(HttpHeaders.class)))
                .thenAnswer(inv -> {
                    HttpHeaders headers = inv.getArgument(1, HttpHeaders.class);
                    headers.add(REFRESH_HEADER, "test-refresh-token");
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer test-access-token");
                    return mockUserResponse;
                });

        String body = objectMapper.writeValueAsString(
                new GoogleLoginRequest("test-code", "http://localhost:3000/oauth/callback")
        );

        mockMvc.perform(post("/api/v1/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(789))
                .andExpect(jsonPath("$.email").value("nostudent@hansei.ac.kr"))
                .andExpect(jsonPath("$.name").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.department").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.isNew").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Refresh Token 헤더로 access token 재발급 성공")
    void refresh_success() throws Exception {
        Mockito.doAnswer(inv -> {
            HttpHeaders headers = inv.getArgument(1, HttpHeaders.class);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer new-access-token");
            return null;
        }).when(authService).refresh(anyString(), any(HttpHeaders.class));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .header(REFRESH_HEADER, "test-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Refresh Token 무효화 성공")
    void logout_success() throws Exception {
        Mockito.doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .header(REFRESH_HEADER, "test-refresh-token"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - refresh 헤더 누락 시 4xx")
    void refresh_missing_header_fail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - refresh 헤더 누락 시 4xx")
    void logout_missing_header_fail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

}
