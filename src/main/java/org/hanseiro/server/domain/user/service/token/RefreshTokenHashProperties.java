package org.hanseiro.server.domain.user.service.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.refresh")
public class RefreshTokenHashProperties {
    private String secret;
}
