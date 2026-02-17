package com.mini.buting.global.constant;

public final class ErrorMessages {

    public static final String INVALID_EMAIL = "올바른 이메일 형식이 아닙니다.";
    public static final String INVALID_PASSWORD = "올바른 비밀번호 형식이 아닙니다.";
    public static final String INVALID_NICKNAME = "올바른 닉네임 형식이 아닙니다.";
    public static final String INVALID_BIO = "자기소개는 공백만 입력할 수 없으며 255자 이하여야 합니다.";
    public static final String INVALID_SIGN_UP_TOKEN = "유효하지 않은 회원가입 토큰입니다.";
    public static final String INVALID_AGE_RANGE = "나이는 17세 이상 100세 이하여야 합니다.";
    public static final String INVALID_ENTRY_YEAR = "학번은 0~99 범위여야 합니다.";

    public static final String EMAIL_NOT_FOUND = "이메일을 입력해주세요.";
    public static final String SCHOOL_EMAIL_NOT_FOUND = "학교 이메일을 입력해주세요.";
    public static final String NICKNAME_NOT_FOUND = "닉네임을 입력해주세요.";
    public static final String VERIFICATION_TYPE_NOT_FOUND = "이메일 검증 타입을 입력해주세요.";
    public static final String VERIFICATION_CODE_NOT_FOUND = "이메일 검증 코드를 입력해주세요.";
    public static final String IS_SOCIAL_NOT_FOUND = "소셜 관련 요청 여부를 입력해주세요.";
    public static final String GENDER_NOT_FOUND = "성별을 입력해주세요.";

    public static final String SIGN_UP_TOKEN_NOT_FOUND = "회원가입 토큰을 입력해주세요.";
    public static final String AGE_NOT_FOUND = "나이를 입력해주세요.";
    public static final String MBTI_NOT_FOUND = "MBTI를 입력해주세요.";
    public static final String ENTRY_YEAR_NOT_FOUND = "학번을 입력해주세요.";

    private ErrorMessages() {
    }
}
