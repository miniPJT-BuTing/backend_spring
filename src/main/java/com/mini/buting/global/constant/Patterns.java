package com.mini.buting.global.constant;

import java.util.regex.Pattern;

/**
 * <h2>공통 정규표현식 상수 관리 클래스</h2>
 *
 * <p>애플리케이션 전반에서 사용되는 유효성 검증용 정규식(Regex)과 프리컴파일된 {@link Pattern} 객체를 관리</p>
 * <p>상수 값을 수정하면 검증 로직 전체에 반영되며, 가독성을 위해 정규식 문자열과 패턴 객체를 쌍으로 구성함</p>
 */
public final class Patterns {

    /**
     * <h3>단순 이메일 검증</h3>
     * <p>규칙: {@code [공백/at 제외] @ [공백/at 제외] . [공백/at 제외]}</p>
     * <p>최소한의 구조적 결함 여부만 체크함</p>
     *
     * @implNote TODO: 학교 전용 도메인(ac.kr 등) 검증 추가가 필요할 수 있음.
     */
    public static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    /**
     * <h3>닉네임 형식 검증 문자열</h3>
     * <p>규칙: 한글, 영문 대소문자, 숫자 포함 2~10자 이내</p>
     *
     * @implNote TODO: 닉네임 규칙에 맞게 설정 필요
     */
    public static final String NICKNAME_REGEX = "^[0-9A-Za-z가-힣]{2,10}$";
    public static final Pattern NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEX);
    /**
     * <h2>비밀번호 형식 검증 문자열</h2>
     * <p>규칙: 8~21자, 영문 대문자, 소문자, 숫자, 특수문자(/!@$~)를 각각 최소 1개 이상 포함</p>
     *
     * @implNote TODO: 비밀번호 규칙에 맞게 설정 필요
     */
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\/!@$~])[A-Za-z\\d\\/!@$~]{8,21}$";
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    /**
     * <h3>공백 여부 검증 문자열</h3>
     * <p>규칙: 공백만으로 이루어지지 않은 최소 1자 이상의 유의미한 문자열</p>
     */
    public static final String NO_WHITESPACE = "^(?=\\s*\\S).*$";

    private Patterns() {
    }
}
