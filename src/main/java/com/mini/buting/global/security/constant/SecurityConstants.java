package com.mini.buting.global.security.constant;

/**
 * <h2>보안 및 인증 관련 전역 상수 정의</h2>
 *
 * <p>애플리케이션 전역에서 사용되는 토큰 정책 및 보안 관련 Redis 키 규칙을 한곳에서 관리</p>
 */
public final class SecurityConstants {

    /**
     * 인스턴스화를 방지하기 위한 private 생성자
     */
    private SecurityConstants() {

    }

    /**
     * <h3>토큰 관련 정책 상수</h3>
     * <p>JWT 발급 및 검증 시 사용되는 표준 규격 정의</p>
     */
    public static final class Token {
        /**
         * 토큰 인증 타입
         */
        public static final String GRANT_TYPE = "Bearer ";
        /**
         * JWT Claim 내 권한 정보 키값
         */
        public static final String AUTHORITIES_CLAIM = "auth";

        // JWT Claim 내 세션 식별자 키값
        public static final String SESSION_ID_CLAIM = "session_uuid";
        /**
         * Refresh Token을 저장할 쿠키의 명칭
         */
        public static final String REFRESH_COOKIE_NAME = "refreshToken";
    }

    /**
     * <h3>Redis Key Prefix 상수</h3>
     * <p>토큰 관리 및 블랙리스트 처리 등을 위한 보안 관련 Redis Key 구조를 정의</p>
     */
    public static final class Redis {
        /**
         * 보안 관련 데이터의 공통 prefix
         */
        private static final String PREFIX = "auth:";

        /**
         * Refresh Token 저장용 prefix
         */
        public static final String REFRESH_PREFIX = PREFIX + "rt:";

        /**
         * 블랙리스트 관련 공통 prefix
         */
        private static final String BLACK_PREFIX = PREFIX + "black-";
        /**
         * 로그아웃된 AccessToken(Blacklist) 저장용 prefix
         */
        public static final String BLACKLIST_ACCESS_PREFIX = BLACK_PREFIX + "at:";
        /**
         * 만료/폐기된 Refresh Token 저장용 prefix
         */
        public static final String BLACKLIST_REFRESH_PREFIX = BLACK_PREFIX + "rt:";
    }

    /**
     * <h3>로그 관련 상수</h3>
     */
    public static final class Log {
        /**
         * 로그 관련 공통 prefix
         */
        public static final String LOG_PREFIX = "[SECURITY] ";
    }
}
