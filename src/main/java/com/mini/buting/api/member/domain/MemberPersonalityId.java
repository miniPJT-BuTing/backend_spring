package com.mini.buting.api.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * MemberPersonality 복합키 클래스
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MemberPersonalityId implements Serializable {

    @Column(name = "member_id")
    private Long memberId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "personality_type", length = 20)
    private PersonalityType personalityType;
}
