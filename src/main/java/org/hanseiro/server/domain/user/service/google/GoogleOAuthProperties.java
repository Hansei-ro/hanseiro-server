package org.hanseiro.server.domain.user.service.google;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Slf4j
@ConfigurationProperties(prefix = "oauth.google")
public class GoogleOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;

    private String tokenUri;
    private String userinfoUri;

    @PostConstruct
    public void logLoadedValues() {
        log.info("[GOOGLE OAUTH CONFIG]");
        log.info("clientId     = {}", mask(clientId));
        log.info("redirectUri  = {}", redirectUri);
        log.info("scope        = {}", scope);
    }

    private String mask(String value) {
        if (value == null || value.length() < 6) return value;
        return value.substring(0, 6) + "****";
    }

}
