package org.hanseiro.server.domain.user.service.google.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "인가 코드는 필수입니다")
        String authorizationCode,

        @NotBlank(message = "리다이렉트 URI는 필수입니다")
        String redirectUri
) {}