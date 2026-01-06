package org.hanseiro.server.domain.user.exception;

import lombok.Getter;

@Getter
public class SocialLoginException extends RuntimeException {
    private final String code;

    public SocialLoginException(String code, String message) {
        super(message);
        this.code = code;
    }
}
