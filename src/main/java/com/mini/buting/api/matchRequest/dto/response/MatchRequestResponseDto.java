package com.mini.buting.api.matchRequest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mini.buting.api.team.domain.Gender;
import com.mini.buting.api.team.domain.TeamSize;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class MatchRequestResponseDto {

    @Builder
    public record MatchRequestCreateResponse(
            Long matchRequestId,
            String status,
            Long requestTeamId,
            Long targetTeamId,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt
    ) {
    }

    @Builder
    public record MatchRequestRespondResponse(
            Long matchRequestId,
            String status,
            Long chatRoomId,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime updatedAt
    ) {
    }

    @Builder
    public record MatchRequestDetailResponse(
            Long matchRequestId,
            String status,
            TeamSummary requestTeam,
            TeamSummary targetTeam,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime updatedAt,
            Boolean expired
    ) {
    }

    @Builder
    public record MatchRequestSummary(
            Long matchRequestId,
            String status,
            String requestedAtAgo,
            String opponentTeamTitle,
            TeamSize opponentTeamSize,
            String opponentPreferredMood,
            Integer opponentPreferredEntryYearMin,
            Integer opponentPreferredEntryYearMax
    ) {
    }

    @Builder
    public record MatchRequestListResponse(
            List<MatchRequestSummary> requests,
            Integer totalCount
    ) {
    }

    @Builder
    public record TeamSummary(
            Long teamId,
            String title,
            TeamSize teamSize,
            Gender gender,
            Integer currentMemberCount,
            Integer targetMemberCount
    ) {
    }
}
