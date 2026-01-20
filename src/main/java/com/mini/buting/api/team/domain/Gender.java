package com.mini.buting.api.team.domain;

import com.mini.buting.api.member.domain.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 팀의 성별 (팀장의 성별에 따라 자동 결정)
 */
@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("남성"),
    FEMALE("여성");

    private final String displayName;

    /**
     * Member의 Gender를 Team의 Gender로 변환
     */
    public static Gender fromMemberGender(com.mini.buting.api.member.domain.Gender memberGender) {
        return switch (memberGender) {
            case M -> MALE;
            case W -> FEMALE;
        };
    }
}


