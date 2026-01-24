package com.mini.buting.global.security.constant;

/**
 * Security 관련 상수 정의
 */
public final class SecurityConstants {

    private SecurityConstants() {

    }

    /* --- token policy --- */
    public static final class Token {
        public static final String GRANT_TYPE = "Bearer ";
        public static final String AUTHORITIES_CLAIM = "auth";
        public static final String REFRESH_COOKIE_NAME = "refreshToken";
    }

    /* --- redis key --- */
    public static final class Redis {
        private static final String PREFIX = "auth:";

        public static final String REFRESH_PREFIX = PREFIX + "rt:";

        // blacklist
        private static final String BLACK_PREFIX = PREFIX + "black-";
        public static final String BLACKLIST_ACCESS_PREFIX = BLACK_PREFIX + "at:";
        public static final String BLACKLIST_REFRESH_PREFIX = BLACK_PREFIX + "rt:";
    }
}
