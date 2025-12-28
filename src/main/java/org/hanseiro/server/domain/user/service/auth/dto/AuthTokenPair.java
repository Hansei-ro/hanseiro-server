package org.hanseiro.server.domain.user.service.auth.dto;

public record AuthTokenPair(String accessToken, String refreshToken) {}
