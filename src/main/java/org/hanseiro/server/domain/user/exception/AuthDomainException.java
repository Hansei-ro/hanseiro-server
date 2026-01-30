package org.hanseiro.server.domain.user.exception;

import org.springframework.http.HttpStatus;

public class AuthDomainException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    private AuthDomainException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static AuthDomainException hanseiEmailOnly() {
        return new AuthDomainException(
                "AUTH_ONLY_HANSEI_ACCOUNT",
                HttpStatus.FORBIDDEN,
                "한세대학교 계정으로 만 사용 가능합니다."
        );
    }

    public static AuthDomainException googleEmailNotVerified() {
        return new AuthDomainException(
                "AUTH_EMAIL_NOT_VERIFIED",
                HttpStatus.FORBIDDEN,
                "이메일 인증이 필요합니다."
        );
    }

    public static AuthDomainException invalidRefreshToken() {
        return new AuthDomainException(
                "AUTH_INVALID_REFRESH_TOKEN",
                HttpStatus.UNAUTHORIZED,
                "인증이 만료되었습니다. 다시 로그인 해주세요."
        );
    }

    public static AuthDomainException googleTokenExchangeFailed() {
        return new AuthDomainException(
                "AUTH_GOOGLE_TOKEN_EXCHANGE_FAILED",
                HttpStatus.UNAUTHORIZED,
                "구글 인증 처리에 실패했습니다"
        );
    }

    public static AuthDomainException googleUserinfoFailed() {
        return new AuthDomainException(
                "AUTH_GOOGLE_USERINFO_FAILED",
                HttpStatus.BAD_GATEWAY,
                "구글 사용자 정보 조회에 실패했습니다"
        );
    }

    public static AuthDomainException tokenIssueFailed() {
        return new AuthDomainException(
                "AUTH_TOKEN_ISSUE_FAILED",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "토큰 발급에 실패했습니다"
        );
    }
}
