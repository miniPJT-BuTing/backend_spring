package com.mini.buting.api.auth.dto.response;

import com.mini.buting.api.university.dto.ResolvedUniversity;
import com.mini.buting.global.mail.dto.MailType;

/**
 * <h2>이메일 인증코드 검증 응답 DTO</h2>
 *
 * <p>이메일 인증 성공 여부와 인증된 이메일의 부가정보(대학 정보 등)를 전달</p>
 * <p>대학 도메인 인증의 경우 해당 대학의 식별자와 명칭을 포함하며,
 * 일반 메일 인증 시 대학 관련 필드는 {@code null}로 반환됨</p>
 *
 * @param verificationType   인증 종류(SIGN_UP 등 {@link MailType}의 이름)
 * @param verified           인증 성공 여부(true: 성공/false: 실패)
 * @param universityDomainId 인증된 대학 도메인의 고유 식별자(대학 인증이 아니면 null)
 * @param universityName     인증된 대학의 국문 명칭(대학 인증이 아니면 null)
 * @param domain             인증에 사용된 실제 이메일 도메인(Ex: pknu.ac.kr)
 */
public record EmailVerifyResponse(
        String verificationType,
        boolean verified,
        Long universityDomainId,
        String universityName,
        String domain
) {
    /**
     * <h3>대학 인증 성공 응답 생성</h3>
     *
     * @param mailType           메일 인증 타입
     * @param verified           검증 결과
     * @param resolvedUniversity 매칭된 대학 정보 객체
     * @return 대학 정보가 포함된 검증 응답 객체
     */
    public static EmailVerifyResponse of(MailType mailType, boolean verified, ResolvedUniversity resolvedUniversity) {
        return new EmailVerifyResponse(
                mailType.name(),
                verified,
                resolvedUniversity.universityDomainId(),
                resolvedUniversity.universityName(),
                resolvedUniversity.domain()
        );
    }

    /**
     * <h3>일반 메일 응답 생성</h3>
     * <p>대학 정보가 필요 없는 일반적인 인증 결과 생성 시 사용</p>
     *
     * @param mailType 메일 인증 타입
     * @param verified 검증 결과
     * @return 대학 정보가 제외된 기본 검증 응답 객체
     */
    public static EmailVerifyResponse of(MailType mailType, boolean verified) {
        return new EmailVerifyResponse(mailType.name(), verified, null, null, null);
    }
}
