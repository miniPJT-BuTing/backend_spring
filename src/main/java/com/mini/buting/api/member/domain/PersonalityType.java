package com.mini.buting.api.member.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 성격 키워드 20가지 유형
 */
@Getter
@AllArgsConstructor
public enum PersonalityType {
    // 에너지 관련
    LIVELINESS("LIVELINESS", "활발함"),
    CALMNESS("CALMNESS", "차분함"),
    ENTHUSIASM("ENTHUSIASM", "열정적"),
    COOLNESS("COOLNESS", "쿨함"),

    // 소통 및 관계
    HONESTY("HONESTY", "솔직함"),
    HUMOR("HUMOR", "유머러스"),
    SOCIABILITY("SOCIABILITY", "사교적"),
    SENSE("SENSE", "센스있음"),
    AFFECTION("AFFECTION", "다정함"),
    CASUALNESS("CASUALNESS", "털털함"),

    // 사고방식
    OPTIMISM("OPTIMISM", "낙천적"),
    EMOTIONALITY("EMOTIONALITY", "감성적"),
    LOGIC("LOGIC", "논리적"),
    POSITIVITY("POSITIVITY", "긍정적"),
    REALISM("REALISM", "현실적"),

    // 행동 성향
    CONSIDERATION("CONSIDERATION", "배려심"),
    SERIOUSNESS("SERIOUSNESS", "진중함"),
    RESPONSIBILITY("RESPONSIBILITY", "책임감"),
    CAUTION("CAUTION", "신중함"),
    LEADERSHIP("LEADERSHIP", "리더십");

    private final String code;
    private final String description;

    /**
     * 문자열을 통한 PersonalityType 생성 (대소문자 구분 없음)
     */
    @JsonCreator
    public static PersonalityType fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BaseException(BaseResponseStatus.PERSONALITY_REQUIRED);
        }

        String upperCode = code.trim().toUpperCase();

        for (PersonalityType type : PersonalityType.values()) {
            if (type.code.equals(upperCode)) {
                return type;
            }
        }

        throw new BaseException(BaseResponseStatus.INVALID_PERSONALITY_TYPE);
    }

    /**
     * JSON 직렬화용
     */
    @JsonValue
    public String getCode() {
        return this.code;
    }

    /**
     * 성격 키워드 코드의 유효성 검증
     */
    public static boolean isValid(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        try {
            fromString(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
