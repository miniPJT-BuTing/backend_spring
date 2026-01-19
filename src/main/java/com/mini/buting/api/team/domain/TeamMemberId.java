package com.mini.buting.api.team.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TeamMemberId implements Serializable {
    private Long team;
    private Long member;
}
