package com.mini.buting.global.security.util;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.principal.AuthUser;

/**
 * <h2>인증 사용자 유효성 검증 유틸리티</h2>
 * <p>Controller/Service 진입 시 인증 사용자 존재 여부를 일관되게 검증하기 위한 공통 유틸</p>
 */
public final class AuthValidator {

    private AuthValidator() {

    }

    /**
     * <h3>인증된 사용자 필수 검증</h3>
     * <p>인증 객체 없으면 {@code AUTHENTICATION_REQUIRED(401)} 예외를 발생</p>
     *
     * @param authUser {@code @AuthenticationPrincipal}로부터 주입받은 사용자 정보 (Nullable)
     * @return {@link AuthUser} 검증이 완료된 신뢰할 수 있는 사용자 객체
     * @throws BaseException 인증 정보가 없는 경우 {@link BaseResponseStatus#AUTHENTICATION_REQUIRED} 발생
     */
    public static AuthUser require(AuthUser authUser) {
        if (authUser == null) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_REQUIRED);
        }
        return authUser;
    }
}
