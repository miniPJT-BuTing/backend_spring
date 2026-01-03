package com.mini.buting.api.member.domain;

import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_social", indexes = {
        @Index(name = "idx_member_social_provider_id", columnList = "provider_id"),
        @Index(name = "idx_member_social_member", columnList = "member_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_social_email", columnNames = {"email"}),
        // 특정 소셜 플랫폼 내에서의 고유 식별값 중복 방지
        @UniqueConstraint(name = "uk_member_social_provider_combination", columnNames = {"provider_name", "provider_id"})
})
@Comment("멤버 소셜 연동 정보 테이블")
public class MemberSocial extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("소셜 로그인 식별자")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_member_social_member"))
    @Comment("멤버 ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_name", nullable = false, length = 20)
    @Comment("소셜 로그인 제공자 (KAKAO 등)")
    private SocialProvider providerName;

    @Column(name = "provider_id", nullable = false, length = 100)
    @Comment("소셜 플랫폼 제공 고유 식별값")
    private String providerId;

    @Column(name = "email", length = 100)
    @Comment("소셜 계정 이메일")
    private String email;

    public static MemberSocial toMemberSocial(Member member, SocialProvider providerName, String providerId, String email) {
        return MemberSocial.builder()
                .member(member)
                .providerName(providerName)
                .providerId(providerId)
                .email(email)
                .build();
    }
}
