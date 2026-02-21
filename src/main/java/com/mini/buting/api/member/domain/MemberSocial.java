package com.mini.buting.api.member.domain;

import com.mini.buting.api.auth.dto.OAuth2SignUpPayload;
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

    /**
     * <h3>MemberSocial 생성</h3>
     * <p>OAuth2 인증 성공 후 전달받은 페이로드를 바탕으로 새로운 소셜 연동 정보를 구축</p>
     *
     * @param member  연동할 서비스 멤버 엔티티
     * @param payload 소셜 플랫폼에서 추출된 식별 정보 DTO
     * @return {@link MemberSocial} 생성된 연동 엔티티
     */
    public static MemberSocial of(Member member, OAuth2SignUpPayload payload) {
        return MemberSocial.builder()
                .member(member)
                .providerName(SocialProvider.from(payload.provider()))
                .providerId(payload.providerId())
                .email(payload.email())
                .build();
    }
}
