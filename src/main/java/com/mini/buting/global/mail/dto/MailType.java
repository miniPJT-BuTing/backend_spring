package com.mini.buting.global.mail.dto;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum MailType {
    /**
     * 회원가입 시 유효 이메일 확인.
     */
    SIGN_UP(
            "[Buting] 이메일 인증을 완료해주세요 \uD83D\uDC93⁼³₌₃",
            "mail/sign-up-verification.html"
    );

    private final String subject;
    private final String templatePath;

    /**
     * 입력된 문자열을 MailType으로 변환
     *
     * @param rawType 메일 타입 문자열
     * @return 매핑된 {@link MailType}
     * @throws BaseException 잘못된 타입일 경우
     */
    public static MailType from(String rawType) {
        if (!StringUtils.hasText(rawType)) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
        try {
            return MailType.valueOf(rawType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BaseException(BaseResponseStatus.UNSUPPORTED_MAIL_TYPE);
        }
    }
}
