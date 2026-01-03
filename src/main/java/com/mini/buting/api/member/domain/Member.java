package com.mini.buting.api.member.domain;

import com.mini.buting.api.analysis.domain.FaceShape;
import com.mini.buting.api.university.domain.Major;
import com.mini.buting.api.university.domain.University;
import com.mini.buting.api.university.domain.UniversityDomain;
import com.mini.buting.global.common.BaseTimeEntity;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_uuid", columnList = "uuid"),
        @Index(name = "idx_member_nickname", columnList = "nickname"),
        @Index(name = "idx_member_university_domain", columnList = "university_domain_id"),
        @Index(name = "idx_member_major", columnList = "major_id"),
        @Index(name = "idx_member_face_shape", columnList = "face_shape_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_uuid", columnNames = {"uuid"}),
        @UniqueConstraint(name = "uk_member_nickname", columnNames = {"nickname"}),
        @UniqueConstraint(name = "uk_university_email", columnNames = {"university_domain_id", "university_email"})
})
@Getter
@Builder
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("사용자 기본 정보 테이블")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("사용자 식별자")
    private Long id;

    @Column(name = "uuid", nullable = false, length = 60)
    @Comment("사용자 고유 UUID")
    private String uuid;

    @Column(name = "nickname", nullable = false, length = 10)
    @Comment("사용자 닉네임")
    private String nickname;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    @Comment("탈퇴 여부 (0: 활성, 1: 탈퇴)")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    @Comment("탈퇴 일시")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Comment("사용자 권한 (GUEST, USER, ADMIN)")
    private MemberRole role;

    @Column(name = "age", nullable = false)
    @Comment("사용자 나이")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 5)
    @Comment("성별 (M: 남성, W: 여성)")
    private Gender gender;

    @Column(name = "bio", length = 255)
    @Comment("자기소개")
    private String bio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_domain_id", nullable = false, foreignKey = @ForeignKey(name = "FK_member_university_domain"))
    @Comment("소속 대학 도메인 ID")
    private UniversityDomain universityDomain;

    @Column(name = "university_email", nullable = false, length = 100)
    @Comment("대학 이메일 계정 아이디 (@ 기호 앞부분)")
    private String universityEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", foreignKey = @ForeignKey(name = "FK_member_major"))
    @Comment("소속 학과 ID")
    private Major major;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_shape_id", foreignKey = @ForeignKey(name = "FK_member_face_shape"))
    @Comment("AI 분석 얼굴형 ID")
    private FaceShape faceShape;

    @PrePersist
    public void prePersist() {
        if (this.role == null) {
            this.role = MemberRole.GUEST;
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    public void softDelete() {
        if (this.isDeleted) {
            throw new BaseException(BaseResponseStatus.MEMBER_ALREADY_DELETED);
        }
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public University getUniversity() {
        return this.universityDomain != null ? this.universityDomain.getUniversity() : null;
    }

    public String getFullUniversityEmail() {
        if (this.universityDomain == null || !StringUtils.hasText(this.universityEmail)) {
            return null;
        }
        return this.universityEmail + "@" + this.universityDomain.getDomain();
    }
}
