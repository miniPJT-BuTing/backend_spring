package com.mini.buting.api.team.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mini.buting.api.team.domain.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class TeamResponseDto {

    @Builder
    public record CreateTeamResponse(Long teamId, String title, String description,
                                     TeamSize teamSize, TeamMood preferredMood,
                                     Integer preferredAgeMin, Integer preferredAgeMax,
                                     Integer preferredEntryYearMin, Integer preferredEntryYearMax,
                                     Boolean isOpen, List<TeamInvitationInfo> sentInvitations) {
    }


    @Builder
    public record TeamInvitationInfo(Long invitationId, Long inviteeId, String inviteeName,
                                     String inviteeNickname, TeamInvitation.InvitationStatus status,
                                     LocalDateTime createdAt, LocalDateTime expiredAt) {
    }


    @Builder
    public record SearchableFriend(Long memberId, String nickname, String universityEmail,
                                   String bio, Integer age, String genderDisplayName,
                                   String universityName, String collegeName,
                                   List<String> personalityTypes) {
    }


    @Builder
    public record SearchFriendsResponse(List<SearchableFriend> friends, Integer totalCount) {
    }


    @Builder
    public record TeamInvitationDetailResponse(Long invitationId, TeamInfo teamInfo,
                                               MemberInfo inviterInfo,
                                               TeamInvitation.InvitationStatus status,
                                               LocalDateTime createdAt, LocalDateTime expiredAt,
                                               Boolean isExpired) {
    }


    @Builder
    public record TeamInfo(Long teamId, String title, String description, TeamSize teamSize,
                           TeamMood preferredMood, Integer preferredAgeMin, Integer preferredAgeMax,
                           Integer preferredEntryYearMin, Integer preferredEntryYearMax,
                           Integer currentMemberCount, Integer targetMemberCount) {
    }


    @Builder
    public record MemberInfo(Long memberId, String nickname, Integer age, String bio,
                             String universityName, String collegeName) {
    }


    @Builder
    public record RespondToInvitationResponse(Long invitationId,
                                              TeamInvitation.InvitationStatus status,
                                              String message) {
    }

    /**
     * 팀 매칭글(=오픈 팀) 목록 아이템
     */
    @Builder
    public record TeamMatchPostSummary(Long teamId, String title, TeamSize teamSize, Gender gender,
                                       String preferredMood,
                                       Integer preferredAgeMin, Integer preferredAgeMax,
                                       Integer preferredEntryYearMin, Integer preferredEntryYearMax,
                                       Integer currentMemberCount, Integer targetMemberCount,
                                       @JsonFormat(shape = JsonFormat.Shape.STRING,
                                                       pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt) {
    }

    /**
     * 팀 매칭글(=오픈 팀) 상세
     */
    @Builder
    public record TeamMatchPostDetail(Long teamId, String title, String description, TeamSize teamSize,
                                      Gender gender, String preferredMood,
                                      Integer preferredAgeMin, Integer preferredAgeMax,
                                      Integer preferredEntryYearMin, Integer preferredEntryYearMax,
                                      Integer currentMemberCount, Integer targetMemberCount,
                                      MemberInfo leaderInfo,
                                      @JsonFormat(shape = JsonFormat.Shape.STRING,
                                                      pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
                                      @JsonFormat(shape = JsonFormat.Shape.STRING,
                                                      pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {
    }
}
