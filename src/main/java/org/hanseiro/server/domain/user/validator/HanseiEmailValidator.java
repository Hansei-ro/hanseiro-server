package org.hanseiro.server.domain.user.validator;

import org.hanseiro.server.domain.user.exception.AuthDomainException;
import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;
import org.springframework.stereotype.Component;

@Component
public class HanseiEmailValidator {
    private static final String HANSEI_DOMAIN = "@hansei.ac.kr";

    public String validateAndGetEmail(GoogleUserInfo userInfo) {
        if (userInfo.emailVerified() == null || !userInfo.emailVerified()) {
            throw AuthDomainException.googleEmailNotVerified();
        }

        String email = (userInfo.email() == null) ? null : userInfo.email().trim().toLowerCase();
        if (email == null || !email.endsWith(HANSEI_DOMAIN)) {
            throw AuthDomainException.hanseiEmailOnly();
        }

        return email;
    }
}
