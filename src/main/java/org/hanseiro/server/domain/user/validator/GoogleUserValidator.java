package org.hanseiro.server.domain.user.validator;

import org.hanseiro.server.domain.user.exception.InvalidSchoolEmailException;
import org.hanseiro.server.domain.user.exception.SocialLoginException;
import org.hanseiro.server.domain.user.service.google.dto.GoogleUserInfo;
import org.springframework.stereotype.Component;

@Component
public class GoogleUserValidator {
    private static final String SCHOOL_DOMAIN = "hansei.ac.kr";

    public void validate(GoogleUserInfo info) {
        if (info == null) {
            throw new SocialLoginException("GOOGLE_USERINFO_NULL", "구글 유저 정보를 가져오지 못했습니다.");
        }

        if (isBlank(info.getId())) {
            throw new SocialLoginException("GOOGLE_ID_MISSING", "구글 사용자 식별자(id)가 없습니다.");
        }

        if (isBlank(info.getEmail())) {
            throw new SocialLoginException("EMAIL_MISSING", "구글 계정 이메일을 확인할 수 없습니다.");
        }

        if (info.getVerifiedEmail() == null || !info.getVerifiedEmail()) {
            throw new SocialLoginException("EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않은 계정입니다.");
        }

        if (!isSchoolEmail(info.getEmail())) {
            throw new InvalidSchoolEmailException("학교 이메일(@hansei.ac.kr)만 로그인할 수 있습니다.");
        }
    }

    private boolean isSchoolEmail(String email) {
        return email.trim().toLowerCase().endsWith("@" + SCHOOL_DOMAIN);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
