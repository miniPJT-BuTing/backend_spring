package com.mini.buting.global.security.constant;

/**
 * <h2>OAuth2 관련 전역 상수</h2>
 */
public class OAuth2Constants {
    private OAuth2Constants() {

    }

    /**
     * OAuth2 인증 흐름에서 사용되는 쿼리 파라미터 키값
     */
    public static final class Parameter {
        public static final String CODE = "code";
        public static final String IS_SUCCESS = "isSuccess";
        public static final String IS_FIRST_LOGIN = "isFirstLogin";
        public static final String PROVIDER_NAME = "providerName";
        public static final String SIGNUP_TOKEN = "signupToken";
        public static final String NICKNAME = "nickname";

    }
}
