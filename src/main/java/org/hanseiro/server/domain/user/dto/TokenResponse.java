package org.hanseiro.server.domain.user.dto;

public record TokenResponse(
        String accessToken,
        Long userId
) {}