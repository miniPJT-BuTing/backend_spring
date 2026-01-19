package com.mini.buting.api.member.domain;

import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * 회원-성격키워드 매핑 테이블
 */
@Entity
@Table(name = "member_personality", indexes = {
        @Index(name = "idx_member_personality_member", columnList = "member_id"),
        @Index(name = "idx_member_personality_type", columnList = "personality_type")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Comment("회원 성격 키워드 매핑 테이블")
public class MemberPersonality extends BaseTimeEntity {

    @EmbeddedId
    private MemberPersonalityId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_member_personality_member"))
    @Comment("회원 ID")
    private Member member;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "personality_type", length = 20, nullable = false, insertable = false, updatable = false)
    @Comment("성격 키워드")
    private PersonalityType personalityType;

    /**
     * MemberPersonality 생성 팩토리 메서드
     */
    public static MemberPersonality of(Member member, PersonalityType personalityType) {
        MemberPersonalityId id = new MemberPersonalityId(member.getId(), personalityType);
        
        return MemberPersonality.builder()
                .id(id)
                .member(member)
                .personalityType(personalityType)
                .build();
    }

    /**
     * 편의 메서드: 성격 키워드 코드 반환
     */
    public String getPersonalityCode() {
        return this.personalityType != null ? this.personalityType.getCode() : null;
    }

    /**
     * 편의 메서드: 성격 키워드 설명 반환
     */
    public String getPersonalityDescription() {
        return this.personalityType != null ? this.personalityType.getDescription() : null;
    }
}
