package org.hanseiro.server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank String authorizationCode,
        @NotBlank String redirectUri
) {}