package org.hanseiro.server.domain.user.exception;

public class InvalidSchoolEmailException extends RuntimeException {
    public InvalidSchoolEmailException(String message) {
        super(message);
    }
}
