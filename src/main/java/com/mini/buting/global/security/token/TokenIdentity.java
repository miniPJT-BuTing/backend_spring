package com.mini.buting.global.security.token;

/**
 * <h2>토큰 인증 정보 식별자</h2>
 * <p>JWT 페이로드에서 추출한 사용자 식별값과 세션 식별값을 하나로 묶어 관리</p>
 *
 * @param subject     회원 고유 식별자(memberUuid)
 * @param sessionUuid 세션 고유 식별자(session_uuid)
 */
public record TokenIdentity(
        String subject,
        String sessionUuid
) {
}
