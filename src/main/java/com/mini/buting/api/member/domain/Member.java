package com.mini.buting.api.member.domain;

import com.mini.buting.api.analysis.domain.FaceShape;
import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.team.domain.TeamMember;
import com.mini.buting.api.university.domain.College; // Major 대신 College(단과대) 사용
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "member", indexes = {
                @Index(name = "idx_member_uuid", columnList = "uuid"),
                @Index(name = "idx_member_nickname", columnList = "nickname"),
                @Index(name = "idx_member_university_domain", columnList = "university_domain_id"),
                @Index(name = "idx_member_college", columnList = "college_id"),
                @Index(name = "idx_member_face_shape", columnList = "face_shape_id"),
                @Index(name = "idx_member_mbti", columnList = "mbti"),
                @Index(name = "idx_member_entry_year", columnList = "entry_year")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "mbti", nullable = false, length = 4)
    @Comment("MBTI 성격 유형 (ENFP, INTJ 등)")
    private MbtiType mbti;

    @Column(name = "entry_year", nullable = false)
    @Comment("학번 (예: 22)")
    private Integer entryYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_domain_id", nullable = false, foreignKey = @ForeignKey(name = "FK_member_university_domain"))
    @Comment("소속 대학 도메인 ID")
    private UniversityDomain universityDomain;

    @Column(name = "university_email", nullable = false, length = 100)
    @Comment("대학 이메일 계정 아이디 (@ 기호 앞부분)")
    private String universityEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", foreignKey = @ForeignKey(name = "FK_member_college"))
    @Comment("소속 단과대 ID")
    private College college;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_shape_id", foreignKey = @ForeignKey(name = "FK_member_face_shape"))
    @Comment("AI 분석 얼굴형 ID")
    private FaceShape faceShape;

    // TeamMember를 통한 다대다 관계
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMemberships = new ArrayList<>();

    // 성격 키워드 관계 (정확히 3개)
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberPersonality> personalities = new ArrayList<>();

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
    
    // 팀 관련 편의 메서드
    public List<Team> getTeams() {
        return teamMemberships.stream()
                        .map(TeamMember::getTeam)
                        .toList();
    }

    public boolean isMemberOf(Team team) {
        return teamMemberships.stream()
                        .anyMatch(tm -> tm.getTeam().equals(team));
    }

    public void joinTeam(Team team) {
        if (isMemberOf(team)) {
            throw new BaseException(BaseResponseStatus.ALREADY_TEAM_MEMBER);
        }
        TeamMember teamMember = TeamMember.of(team, this);
        this.teamMemberships.add(teamMember);
    }

    public void leaveTeam(Team team) {
        if (!isMemberOf(team)) {
            throw new BaseException(BaseResponseStatus.NOT_TEAM_MEMBER);
        }
        this.teamMemberships.removeIf(tm -> tm.getTeam().equals(team));
    }

    // MBTI 관련 편의 메서드
    public void updateMbti(MbtiType newMbti) {
        if (newMbti == null) {
            throw new BaseException(BaseResponseStatus.MBTI_REQUIRED);
        }
        this.mbti = newMbti;
    }

    public String getMbtiCode() {
        return this.mbti != null ? this.mbti.getCode() : null;
    }

    public String getMbtiDescription() {
        return this.mbti != null ? this.mbti.getDescription() : null;
    }

    // 성격 키워드 관련 편의 메서드
    public void setPersonalities(List<PersonalityType> personalityTypes) {
        if (personalityTypes == null) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
        if (personalityTypes.size() != 3) {
            throw new BaseException(BaseResponseStatus.INVALID_PERSONALITY_COUNT);
        }

        // 중복 체크
        Set<PersonalityType> uniqueTypes = Set.copyOf(personalityTypes);
        if (uniqueTypes.size() != 3) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_PERSONALITY_TYPES);
        }

        // 기존 성격 키워드 삭제 후 새로 추가
        this.personalities.clear();
        personalityTypes.forEach(type ->
                        this.personalities.add(MemberPersonality.of(this, type))
        );
    }

    public List<PersonalityType> getPersonalityTypes() {
        return personalities.stream()
                        .map(MemberPersonality::getPersonalityType)
                        .toList();
    }

    public List<String> getPersonalityCodes() {
        return personalities.stream()
                        .map(MemberPersonality::getPersonalityCode)
                        .toList();
    }

    public List<String> getPersonalityDescriptions() {
        return personalities.stream()
                        .map(MemberPersonality::getPersonalityDescription)
                        .toList();
    }

    public boolean hasPersonalityType(PersonalityType personalityType) {
        return personalities.stream()
                        .anyMatch(mp -> mp.getPersonalityType().equals(personalityType));
    }

    // 📌 얼굴형 분석 결과 편의 메서드
    public String getFaceShapeName() {
        return this.faceShape != null ? this.faceShape.getName() : null;
    }
}
