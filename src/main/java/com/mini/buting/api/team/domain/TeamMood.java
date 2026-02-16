package com.mini.buting.api.team.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 팀의 선호 분위기
 */
@Getter
public enum TeamMood {
    ROMANTIC_TENSION("연애 텐션"), FRIENDSHIP_TENSION("친구 텐션"), FLIRTY_TENSION("썸 텐션"), CALM_TENSION(
                    "차분 텐션"), HIGH_TENSION("하이 텐션"), DRINKING_TENSION("술 텐션"), EMOTIONAL_TENSION(
                    "감성 텐션"), ANY_MOOD("어떤 분위기든 상관없음");

    private final String description;

    TeamMood(String description) {
        this.description = description;
    }

    // 화면에 표시할 한글명 반환
    public String getDisplayName() {
        return this.description;
    }
}
