package org.hanseiro.server.domain.user.service.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfo(
        String id,              // sub?
        String email,
        @JsonProperty("email_verified") Boolean emailVerified,
        String name
) {}