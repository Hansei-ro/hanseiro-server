package org.hanseiro.server.domain.user.service.token;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class RefreshTokenHasher {

    private final RefreshTokenHashProperties props;

    public RefreshTokenHasher(RefreshTokenHashProperties props) {
        this.props = props;
    }

    public String hash(String rawRefreshToken) {
        if (rawRefreshToken == null) return null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(
                    props.getSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(key);
            byte[] result = mac.doFinal(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Refresh token hashing failed", e);
        }
    }
}
