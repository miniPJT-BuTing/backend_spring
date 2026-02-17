package com.mini.buting.global.mail.constants;

import java.time.Duration;

/**
 * <h2>메일 도메인 공통 상수</h2>
 */
public final class MailConstants {
    private MailConstants() {
    }

    /**
     * <h3>메일 템플릿 치환 키</h3>
     * <p>템플릿 렌더링 시 Map Key로 사용</p>
     */
    public static final class Key {
        public static final String MAIN_URL = "mainUrl";
        public static final String LOGO_IMAGE_URL = "logoImageUrl";
        public static final String CODE = "code";

        private Key() {
        }
    }

    /**
     * <h3>인증 코드 정책</h3>
     */
    public static final class Verification {
        public static final int CODE_LENGTH = 6;
        public static final Duration EXPIRE_DURATION = Duration.ofMinutes(5);

        // 같은 이메일/타입 재발급 쿨다운
        public static final Duration ISSUE_COOLDOWN_DURATION = Duration.ofSeconds(60);

        // 검증 최대 시도 횟수
        public static final int MAX_VERIFY_ATTEMPTS = 5;

        private Verification() {
        }
    }

    /**
     * <h3>메일 Redis 키 규칙</h3>
     */
    public static final class Redis {
        private static final String BASE_PREFIX = "mail:";
        public static final String VERIFICATION_CODE_PREFIX = BASE_PREFIX + "verification:";
        public static final String VERIFICATION_COOLDOWN_PREFIX = VERIFICATION_CODE_PREFIX + "cooldown:";
        public static final String VERIFICATION_ATTEMPT_PREFIX = VERIFICATION_CODE_PREFIX + "attempt:";

        private Redis() {
        }
    }

    /**
     * <h3>로그 관련 상수</h3>
     */
    public static final class Log {
        /**
         * 로그 관련 공통 prefix
         */
        public static final String LOG_PREFIX = "[SMTP] ";

        private Log() {
        }
    }
}
