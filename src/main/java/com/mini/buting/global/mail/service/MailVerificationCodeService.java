package com.mini.buting.global.mail.service;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.mail.constants.MailConstants;
import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.mail.dto.verification.VerificationCode;
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
 * <h2>메일 인증코드 발급 및 검증 서비스</h2>
 * <p>이메일 인증 과정에서 필요한 보안 코드의 생명 주기를 관리</p>
 * <hr/>
 * <h5>보안 관련 사항</h5>
 * <ul>
 *     <li>연사 방지(Cooldown 적용): 동일 이메일/타입에 대해 짧은 시간 내 반복 발급 차단</li>
 *     <li>무차별 대입 방지: 잘못된 코드 입력 시도 횟수를 차단하고, 초과 시 즉시 코드를 파기함.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailVerificationCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedisUtils redisUtils;

    /**
     * <h3>인증코드 신규 발급</h3>
     * <ul>
     *     <li>대상 이메일로 새로운 인증코드를 생성하고 Redis에 저장</li>
     *     <li>이전에 발급된 시도 횟수 정보가 있다면 초기화</li>
     *     <li>설정된 Cooldown 시간이 지나야 재발급 가능</li>
     * </ul>
     *
     * @param email    정규화 대상 이메일
     * @param mailType 메일 서비스 타입(SIGN_UP 등)
     * @return {@link VerificationCode} 생성된 코드와 만료 시각 정보
     * @throws BaseException Cooldown 미경과 시 {@code MAIL_VERIFICATION_COOLDOWN} 발생
     */
    public VerificationCode issueVerificationCode(String email, MailType mailType) {
        validateIssueRequest(email, mailType);

        String normalizedEmail = normalizedEmail(email);
        String cooldownKey = buildCooldownKey(normalizedEmail, mailType);

        // 발급 쿨다운 체크
        boolean acquired = redisUtils.setValueIfAbsent(cooldownKey, "1", MailConstants.Verification.ISSUE_COOLDOWN_DURATION);
        if (!acquired) {
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_COOLDOWN);
        }

        // 신규 코드 생성 및 저장
        String plainCode = generateNumericCode(MailConstants.Verification.CODE_LENGTH);
        String verificationKey = buildVerificationKey(normalizedEmail, mailType);
        String attemptKey = buildAttemptKey(normalizedEmail, mailType);

        redisUtils.setValue(verificationKey, plainCode, MailConstants.Verification.EXPIRE_DURATION);
        redisUtils.deleteValue(attemptKey); // 이전 시도 횟수 초기화

        String expiredAt = LocalDateTime.now()
                .plus(MailConstants.Verification.EXPIRE_DURATION)
                .format(DateTimeFormatter.ISO_DATE_TIME);

        return VerificationCode.of(plainCode, expiredAt);
    }

    /**
     * <h3>인증코드 유효성 검증</h3>
     * <ul>
     *     <li>사용자 입력 코드와 Redis에 저장된 코드를 대조</li>
     *     <li>검증 성공 시 즉시 코드를 파기(일회성)</li>
     *     <li>시도 횟수 초과 시 보안을 위해 해당 세션을 즉시 만료시킴</li>
     * </ul>
     *
     * @param email     대상 이메일
     * @param mailType  메일 서비스 타입
     * @param inputCode 사용자가 입력한 인증코드
     * @return {@code true} 검증 성공 시
     * @throws BaseException 코드만료, 불일치, 시도횟수 초과 시 각각의 상태 코드 발생
     */
    public boolean verifyCode(String email, MailType mailType, String inputCode) {
        if (!StringUtils.hasText(email) || mailType == null || !StringUtils.hasText(inputCode)) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        String normalizedEmail = normalizedEmail(email);
        String verificationKey = buildVerificationKey(normalizedEmail, mailType);
        String attemptKey = buildAttemptKey(normalizedEmail, mailType);

        // 저장된 코드 존재 여부 확인
        Object savedObj = redisUtils.getValue(verificationKey);
        if (!(savedObj instanceof String savedHash) || !StringUtils.hasText(savedHash)) {
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_EXPIRED);
        }

        // 시도 횟수 확인 및 증가
        int currentAttempts = getAttemptCount(attemptKey);
        validateAttemptLimit(currentAttempts, verificationKey, attemptKey);

        // 일치 여부 판별
        if (savedHash.equals(inputCode.trim())) {
            redisUtils.deleteValue(verificationKey);
            redisUtils.deleteValue(attemptKey);
            return true;
        }

        // 불일치 시 시도 횟수 갱신 및 정책 적용
        updateAttemptCount(attemptKey, currentAttempts + 1, verificationKey);
        throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_MISMATCH);
    }

    // --- Helper Methods ---

    private void updateAttemptCount(String attemptKey, int nextAttempts, String verificationKey) {
        redisUtils.setValue(attemptKey, String.valueOf(nextAttempts), MailConstants.Verification.EXPIRE_DURATION);
        if (nextAttempts >= MailConstants.Verification.MAX_VERIFY_ATTEMPTS) {
            redisUtils.deleteValue(verificationKey);
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_ATTEMPTS_EXCEEDED);
        }
    }

    private void validateAttemptLimit(int attempts, String verificationKey, String attemptKey) {
        if (attempts >= MailConstants.Verification.MAX_VERIFY_ATTEMPTS) {
            redisUtils.deleteValue(verificationKey);
            redisUtils.deleteValue(attemptKey);
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_ATTEMPTS_EXCEEDED);
        }
    }

    private void validateIssueRequest(String email, MailType mailType) {
        if (!StringUtils.hasText(email) || mailType == null) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
    }

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
     */
    private String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
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
}
