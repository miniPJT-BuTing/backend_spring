package com.mini.buting.global.mail.dto;

/**
 * <h2>메일 인증코드 응답 DTO</h2>
 *
 * @param code      발급된 인증 코드
 * @param expiredAt 만료 시각(ISO-8601 문자열)
 */
public record VerificationCode(
        String code,
        String expiredAt
) {
    /**
     * 인증코드 DTO 생성 팩토리
     *
     * @param code      인증코드
     * @param expiredAt 만료 시각 문자열
     * @return {@link VerificationCode}
     */
    public static VerificationCode of(String code, String expiredAt) {
        return new VerificationCode(code, expiredAt);
    }
}
