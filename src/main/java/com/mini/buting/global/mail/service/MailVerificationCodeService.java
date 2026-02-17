package com.mini.buting.global.mail.service;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.mail.constants.MailConstants;
import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.mail.dto.VerificationCode;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * <h2>메일 인증코드 발급/검증 서비스</h2>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailVerificationCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedisUtils redisUtils;

    /**
     * 이메일 인증코드를 발급하고 Redis에 해시 형태로 저장
     *
     * @param email    대상 이메일
     * @param mailType 메일 타입
     * @return 발급 코드와 만료 시각
     */
    public VerificationCode issueVerificationCode(String email, MailType mailType) {
        validateIssueRequest(email, mailType);

        String normalizedEmail = normalizedEmail(email);
        String cooldownKey = buildCooldownKey(normalizedEmail, mailType);

        boolean acquired = redisUtils.setValueIfAbsent(cooldownKey, "1", MailConstants.Verification.ISSUE_COOLDOWN_DURATION);

        if (!acquired) {
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_COOLDOWN);
        }

        String plainCode = generateNumericCode(MailConstants.Verification.CODE_LENGTH);
        String verificationKey = buildVerificationKey(normalizedEmail, mailType);
        String attemptKey = buildAttemptKey(normalizedEmail, mailType);

        redisUtils.setValue(verificationKey, plainCode, MailConstants.Verification.EXPIRE_DURATION);
        redisUtils.setValue(cooldownKey, "1", MailConstants.Verification.ISSUE_COOLDOWN_DURATION);
        redisUtils.deleteValue(attemptKey);

        String expiredAt = LocalDateTime.now()
                .plus(MailConstants.Verification.EXPIRE_DURATION)
                .format(DateTimeFormatter.ISO_DATE_TIME);

        return VerificationCode.of(plainCode, expiredAt);
    }

    /**
     * 인증코드 검증
     *
     * @param email     대상 이메일
     * @param mailType  메일 타입
     * @param inputCode 입력 코드
     * @return 검증 성공 여부(true: 일치, false: 불일치)
     * @implNote 검증 성공 시 Redis 코드를 삭제해 재사용 차단
     */
    public boolean verifyCode(String email, MailType mailType, String inputCode) {
        if (!StringUtils.hasText(email) || mailType == null || !StringUtils.hasText(inputCode)) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        String normalizedEmail = normalizedEmail(email);
        String verificationKey = buildVerificationKey(normalizedEmail, mailType);
        String attemptKey = buildAttemptKey(normalizedEmail, mailType);

        Object savedObj = redisUtils.getValue(verificationKey);
        if (!(savedObj instanceof String savedHash) || !StringUtils.hasText(savedHash)) {
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_EXPIRED);
        }

        int currentAttempts = getAttemptCount(attemptKey);
        if (currentAttempts >= MailConstants.Verification.MAX_VERIFY_ATTEMPTS) {
            redisUtils.deleteValue(verificationKey);
            redisUtils.deleteValue(attemptKey);
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_ATTEMPTS_EXCEEDED);
        }

        boolean matched = savedHash.equals(inputCode.trim());

        if (matched) {
            redisUtils.deleteValue(verificationKey);
            redisUtils.deleteValue(attemptKey);
            return true;
        }

        int nextAttempts = currentAttempts + 1;
        redisUtils.setValue(attemptKey, String.valueOf(nextAttempts), MailConstants.Verification.EXPIRE_DURATION);

        if (nextAttempts >= MailConstants.Verification.MAX_VERIFY_ATTEMPTS) {
            redisUtils.deleteValue(verificationKey);
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_ATTEMPTS_EXCEEDED);
        }

        throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_MISMATCH);
    }

    // --- Helper Methods ---

    /**
     * 인증코드 발급 입력값 검증
     */
    private void validateIssueRequest(String email, MailType mailType) {
        if (!StringUtils.hasText(email) || mailType == null) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
    }

    /**
     * 인증코드 Redis 키 생성
     */
    private String buildVerificationKey(String normalizedEmail, MailType mailType) {
        return MailConstants.Redis.VERIFICATION_CODE_PREFIX + mailType.name() + ":" + normalizedEmail;
    }

    /**
     * 인증코드 발급 cooldown Redis 키 생성
     */
    private String buildCooldownKey(String normalizedEmail, MailType mailType) {
        return MailConstants.Redis.VERIFICATION_COOLDOWN_PREFIX + mailType.name() + ":" + normalizedEmail;
    }

    /**
     * 인증코드 검증 시도 횟수 Redis 키 생성
     */
    private String buildAttemptKey(String normalizedEmail, MailType mailType) {
        return MailConstants.Redis.VERIFICATION_ATTEMPT_PREFIX + mailType.name() + ":" + normalizedEmail;
    }

    /**
     * 검증 시도 횟수 조회
     */
    private int getAttemptCount(String attemptKey) {
        Object value = redisUtils.getValue(attemptKey);
        if (!(value instanceof String countText) || !StringUtils.hasText(countText)) {
            return 0;
        }

        try {
            return Integer.parseInt(countText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 이메일 문자열 정규화
     *
     * @param email 원본 이메일
     * @return 소문자/trim 처리된 이메일
     */
    private String normalizedEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 숫자 인증코드 생성
     *
     * @param length 코드 길이
     * @return 숫자 코드 문자열
     */
    private String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }
}
