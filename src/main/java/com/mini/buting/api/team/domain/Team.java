package com.mini.buting.api.team.domain;

import com.mini.buting.api.matchRequest.domain.MatchRequest;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.global.common.BaseTimeEntity;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Team", indexes = {
                // 매칭 가능한 팀 조회 (가장 빈번한 조회 - is_open과 최신순)
                @Index(name = "idx_team_open_created", columnList = "is_open, created_at DESC"),

                // 팀 검색 필터링 조합 인덱스 (성별 + 크기 + 분위기)
                @Index(name = "idx_team_search_filter",
                                columnList = "is_open, gender, team_size, preferred_mood"),

                // 연령대 범위 검색용 인덱스
                @Index(name = "idx_team_age_range",
                                columnList = "is_open, preferred_age_min, preferred_age_max"),

                // 팀장별 팀 조회 (내 팀 관리용)
                @Index(name = "idx_team_leader", columnList = "leader_id"),

                // 최신순 정렬용 (전체 팀 목록)
                @Index(name = "idx_team_created", columnList = "created_at DESC"),

                // 성별별 조회
                @Index(name = "idx_team_gender_open", columnList = "gender, is_open"),

                // 팀 크기별 조회
                @Index(name = "idx_team_size_open", columnList = "team_size, is_open"),

                // 분위기별 조회
                @Index(name = "idx_team_mood_open", columnList = "preferred_mood, is_open")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "preferred_age_min", nullable = false)
    private Byte preferredAgeMin;

    @Column(name = "preferred_age_max", nullable = false)
    private Byte preferredAgeMax;

    @Column(name = "preferred_entry_year_min", nullable = false)
    private Byte preferredEntryYearMin;

    @Column(name = "preferred_entry_year_max", nullable = false)
    private Byte preferredEntryYearMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_size", nullable = false)
    private TeamSize teamSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.mini.buting.api.team.domain.Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_mood", nullable = false)
    private TeamMood preferredMood;

    @Column(nullable = false)
    private String description;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen;

    // 연관관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private Member leader;

    // TeamMember 중간 테이블을 통한 다대다 관계
    @OneToMany(mappedBy = "team")
    private List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "requestTeam")
    private List<MatchRequest> sentMatchRequests = new ArrayList<>();

    @OneToMany(mappedBy = "targetTeam")
    private List<MatchRequest> receivedMatchRequests = new ArrayList<>();

    @Builder
    public Team(String title, Byte preferredAgeMin, Byte preferredAgeMax,
                    Byte preferredEntryYearMin, Byte preferredEntryYearMax, TeamSize teamSize,
                    com.mini.buting.api.team.domain.Gender gender, TeamMood preferredMood, String description, Boolean isOpen,
                    Member leader) {
        this.title = title;
        this.preferredAgeMin = preferredAgeMin;
        this.preferredAgeMax = preferredAgeMax;
        this.preferredEntryYearMin = preferredEntryYearMin;
        this.preferredEntryYearMax = preferredEntryYearMax;
        this.teamSize = teamSize;
        this.gender = gender;
        this.preferredMood = preferredMood;
        this.description = description;
        this.isOpen = isOpen;
        this.leader = leader;
    }

    // 비즈니스 메서드
    public void updateIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void activate() {
        this.isOpen = true;
    }

    public int getCurrentMemberCount() {
        return teamMembers.size();
    }

    public boolean isFullTeam() {
        return getCurrentMemberCount() >= teamSize.getSize();
    }

    public boolean canRequestMatch() {
        return isFullTeam() && isOpen;
    }

    // 팀원 관리 메서드
    public void addMember(Member member) {
        if (isFullTeam()) {
            throw new BaseException(BaseResponseStatus.TEAM_FULL);
        }
        TeamMember teamMember = TeamMember.of(this, member);
        this.teamMembers.add(teamMember);
    }

    public void removeMember(Member member) {
        this.teamMembers.removeIf(tm -> tm.getMember().equals(member));
    }

    public List<Member> getMembers() {
        return teamMembers.stream().map(TeamMember::getMember).toList();
    }

    public boolean isMember(Member member) {
        return teamMembers.stream().anyMatch(tm -> tm.getMember().equals(member));
    }
}
