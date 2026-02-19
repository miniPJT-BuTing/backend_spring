package com.mini.buting.api.university.util;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class UniversityEmailParser {
    /**
     * 이메일에서 도메인을 추출한다.
     *
     * @param email 원본 이메일
     * @return 소문자 도메인 문자열
     */
    public String extractDomain(String email) {
        String normalized = normalize(email);
        int at = normalized.lastIndexOf('@');
        if (at < 1 || at == normalized.length() - 1) {
            throw new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL);
        }
        return normalized.substring(at + 1);
    }

    /**
     * 이메일에서 @ 앞 local-part를 추출한다.
     *
     * @param email 원본 이메일
     * @return 소문자 local-part 문자열
     */
    public String extractLocalPart(String email) {
        String normalized = normalize(email);
        int at = normalized.lastIndexOf('@');
        if (at < 1) {
            throw new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL);
        }
        return normalized.substring(0, at);
    }

    private String normalize(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL);
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
