package com.mini.buting.api.team.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor; /**
 * 팀 크기
 */
@Getter
@RequiredArgsConstructor
public enum TeamSize {
    TWO_ON_TWO("2:2", 2),
    THREE_ON_THREE("3:3", 3),
    FOUR_ON_FOUR("4:4", 4),
    FIVE_ON_FIVE("5:5", 5),
    SIX_ON_SIX("6:6", 6);

    private final String displayName;
    private final int size;
}
