package com.mini.buting.api.auth.dto.response;

import com.mini.buting.global.mail.constants.MailConstants;
import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.mail.dto.verification.VerificationCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <h2>이메일 코드 생성/전송 응답 DTO</h2>
 *
 * @param expiredAt 코드 만료 시각(ISO-8601 문자열)
 */
public record EmailCodeResponse(
        String verificationType,
        String serverTime,
        String expiredAt,
        long expiredInSeconds,
        String resendAvailableAt,
        long resendRemainingSeconds,
        int maxVerifyAttempts
) {
    public static EmailCodeResponse of(
            MailType mailType,
            VerificationCode verificationCode,
            LocalDateTime now
    ) {
        LocalDateTime resenAvailableAt = now.plus(MailConstants.Verification.ISSUE_COOLDOWN_DURATION);

        return new EmailCodeResponse(
                mailType.name(),
                now.format(DateTimeFormatter.ISO_DATE_TIME),
                verificationCode.expiredAt(),
                MailConstants.Verification.EXPIRE_DURATION.getSeconds(),
                resenAvailableAt.format(DateTimeFormatter.ISO_DATE_TIME),
                MailConstants.Verification.ISSUE_COOLDOWN_DURATION.getSeconds(),
                MailConstants.Verification.MAX_VERIFY_ATTEMPTS
        );
    }
}
