package com.mini.buting.api.member.domain;

import com.mini.buting.api.analysis.domain.FaceShape;
import com.mini.buting.api.member.constants.MemberConstants;
import com.mini.buting.api.member.dto.request.SignUpRequest;
import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.team.domain.TeamMember;
import com.mini.buting.api.university.domain.College;
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
@Table(name = "member", indexes = {@Index(name = "idx_member_uuid", columnList = "uuid"),
        @Index(name = "idx_member_nickname", columnList = "nickname"),
        @Index(name = "idx_member_university_domain", columnList = "university_domain_id"),
        @Index(name = "idx_member_college", columnList = "college_id"),
        @Index(name = "idx_member_face_shape", columnList = "face_shape_id"),
        @Index(name = "idx_member_mbti", columnList = "mbti"),
        @Index(name = "idx_member_entry_year", columnList = "entry_year")},
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_uuid", columnNames = {"uuid"}),
                @UniqueConstraint(name = "uk_member_nickname",
                        columnNames = {"nickname"}),
                @UniqueConstraint(name = "uk_university_email",
                        columnNames = {"university_domain_id",
                                "university_email"})})
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
    @JoinColumn(name = "university_domain_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_member_university_domain"))
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

    /**
     * <h3>신규 Member 엔티티 생성 팩토리 메서드</h3>
     * <p>회원가입 요청 DTO를 기반으로 초기 권한({@code USER})과 필수 대학 정보를 조합하여 엔티티를 빌드</p>
     */
    public static Member of(SignUpRequest requestDto, UniversityDomain universityDomain, College college, FaceShape faceShape, String uuid, String localPart) {
        return Member.builder()
                .uuid(uuid)
                .nickname(requestDto.nickname())
                .role(MemberRole.USER)
                .age(requestDto.age())
                .gender(requestDto.gender())
                .bio(requestDto.bio())
                .mbti(requestDto.mbti())
                .entryYear(requestDto.entryYear())
                .universityDomain(universityDomain)
                .universityEmail(localPart)
                .college(college)
                .faceShape(faceShape)
                .build();
    }

    @PrePersist
    public void prePersist() {
        if (this.role == null) {
            this.role = MemberRole.GUEST;
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    /**
     * <h3>Soft Delete 수행</h3>
     *
     * @throws BaseException 이미 탈퇴한 회원인 경우 {@code MEMBER_ALREADY_DELETED} 발생
     */
    public void softDelete() {
        if (this.isDeleted) {
            throw new BaseException(BaseResponseStatus.MEMBER_ALREADY_DELETED);
        }
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // --- 대학 및 이메일 도메인 ---
    public University getUniversity() {
        return this.universityDomain != null ? this.universityDomain.getUniversity() : null;
    }

    /**
     * @return {@code id@email.ac.kr} 형태의 전체 학교 이메일 주소 반환
     */
    public String getFullUniversityEmail() {
        if (this.universityDomain == null || !StringUtils.hasText(this.universityEmail)) {
            return null;
        }
        return this.universityEmail + "@" + this.universityDomain.getDomain();
    }

    // --- 팀(Team) 관련 편의 메서드 ---
    public List<Team> getTeams() {
        return teamMemberships.stream().map(TeamMember::getTeam).toList();
    }

    public boolean isMemberOf(Team team) {
        return teamMemberships.stream().anyMatch(tm -> tm.getTeam().equals(team));
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

    // --- 성격 및 MBTI 관련 ---
    public void updateNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
        this.nickname = nickname.trim();
    }

    public void updateBio(String bio) {
        if (!StringUtils.hasText(bio)) {
            this.bio = null;
            return;
        }
        this.bio = bio.trim();
    }

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

    /**
     * <h3>사용자 성격 키워드 일괄 설정</h3>
     * <p>기존 키워드 컬렉션과 diff를 계산해 삭제/추가만 수행</p>
     * <p>clear 후 동일 PK 재삽입 시 발생할 수 있는 영속성 컨텍스트 충돌을 방지</p>
     *
     * @param personalityTypes 설정할 키워드 목록 (최대 {@link MemberConstants.Personality#MAX_COUNT}개)
     * @throws BaseException 개수 초과, 중복 타입, 혹은 필수값 누락 시 관련 에러 발생
     */
    public void setPersonalities(List<PersonalityType> personalityTypes) {
        if (personalityTypes == null || personalityTypes.isEmpty()) {
            throw new BaseException(BaseResponseStatus.PERSONALITY_REQUIRED);
        }
        if (personalityTypes.size() > MemberConstants.Personality.MAX_COUNT) {
            throw new BaseException(BaseResponseStatus.INVALID_PERSONALITY_COUNT);
        }

        // 중복 체크
        Set<PersonalityType> uniqueTypes = Set.copyOf(personalityTypes);
        if (uniqueTypes.size() != personalityTypes.size()) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_PERSONALITY_TYPES);
        }

        // 1) 요청 목록에 없는 기존 항목 제거
        this.personalities.removeIf(mp -> !uniqueTypes.contains(mp.getPersonalityType()));

        // 2) 이미 존재하는 항목은 유지하고, 누락된 항목만 추가
        Set<PersonalityType> existingTypes = this.personalities.stream()
                .map(MemberPersonality::getPersonalityType)
                .collect(java.util.stream.Collectors.toSet());

        uniqueTypes.stream()
                .filter(type -> !existingTypes.contains(type))
                .forEach(type -> this.personalities.add(MemberPersonality.of(this, type)));
    }

    public List<PersonalityType> getPersonalityTypes() {
        return personalities.stream().map(MemberPersonality::getPersonalityType).toList();
    }

    public List<String> getPersonalityCodes() {
        return personalities.stream().map(MemberPersonality::getPersonalityCode).toList();
    }

    public List<String> getPersonalityDescriptions() {
        return personalities.stream().map(MemberPersonality::getPersonalityDescription).toList();
    }

    public boolean hasPersonalityType(PersonalityType personalityType) {
        return personalities.stream()
                .anyMatch(mp -> mp.getPersonalityType().equals(personalityType));
    }

    // 얼굴형 분석 결과 편의 메서드
    public String getFaceShapeName() {
        return this.faceShape != null ? this.faceShape.getName() : null;
    }

    public void updateFaceShape(FaceShape faceShape) {
        if (faceShape != null) {
            this.faceShape = faceShape;
        }
    }
}
