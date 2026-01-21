package com.mini.buting.api.team.dto.response;

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
}
