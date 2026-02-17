package com.mini.buting.global.security.token;

/**
 * <h2>토큰 식별 정보</h2>
 *
 * @param subject     memberUuid
 * @param sessionUuid session_uuid
 */
public record TokenIdentity(
        String subject,
        String sessionUuid
) {
}
