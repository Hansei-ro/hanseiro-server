package org.hanseiro.server.domain.user.service.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleUserInfo {
    private String id;

    private String email;

    @JsonProperty("verified_email")
    private Boolean verifiedEmail;

    private String name;
}
