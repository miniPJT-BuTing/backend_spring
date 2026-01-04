package com.mini.buting.api.member.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MBTI 16가지 성격 유형
 */
@Getter
@AllArgsConstructor
public enum MbtiType {
    // 분석가 (NT)
    INTJ("INTJ", "건축가"),
    INTP("INTP", "논리술사"),
    ENTJ("ENTJ", "통솔자"),
    ENTP("ENTP", "변론가"),
    
    // 외교관 (NF)
    INFJ("INFJ", "옹호자"),
    INFP("INFP", "중재자"),
    ENFJ("ENFJ", "선도자"),
    ENFP("ENFP", "활동가"),
    
    // 관리자 (SJ)
    ISTJ("ISTJ", "현실주의자"),
    ISFJ("ISFJ", "수호자"),
    ESTJ("ESTJ", "경영자"),
    ESFJ("ESFJ", "집정관"),
    
    // 탐험가 (SP)
    ISTP("ISTP", "만능재주꾼"),
    ISFP("ISFP", "모험가"),
    ESTP("ESTP", "사업가"),
    ESFP("ESFP", "연예인");

    private final String code;
    private final String description;

    /**
     * 문자열을 통한 MBTI 타입 생성 (대소문자 구분 없음)
     */
    @JsonCreator
    public static MbtiType fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BaseException(BaseResponseStatus.MBTI_REQUIRED);
        }
        
        String upperCode = code.trim().toUpperCase();
        
        for (MbtiType type : MbtiType.values()) {
            if (type.code.equals(upperCode)) {
                return type;
            }
        }

        throw new BaseException(BaseResponseStatus.INVALID_MBTI_FORMAT);
    }
    
    /**
     * JSON 직렬화용
     */
    @JsonValue
    public String getCode() {
        return this.code;
    }
    
    /**
     * 4글자 MBTI 코드의 유효성 검증
     */
    public static boolean isValid(String code) {
        if (code == null || code.length() != 4) {
            return false;
        }
        
        try {
            fromString(code);
            return true;
        } catch (BaseException  e) {
            return false;
        }
    }
    
    /**
     * 각 차원별 특성 반환
     */
    public char getEnergyDirection() {
        return code.charAt(0); // E or I
    }
    
    public char getInformationProcessing() {
        return code.charAt(1); // S or N
    }
    
    public char getDecisionMaking() {
        return code.charAt(2); // T or F
    }
    
    public char getLifestyle() {
        return code.charAt(3); // J or P
    }
}
