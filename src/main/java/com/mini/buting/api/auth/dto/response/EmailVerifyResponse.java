package com.mini.buting.api.auth.dto.response;

import com.mini.buting.global.mail.dto.MailType;

/**
 * <h2>이메일 인증코드 검증 응답 DTO</h2>
 */
public record EmailVerifyResponse(
        String verificationType,
        boolean verified
) {
    public static EmailVerifyResponse of(MailType mailType, boolean verified) {
        return new EmailVerifyResponse(mailType.name(), verified);
    }
}
