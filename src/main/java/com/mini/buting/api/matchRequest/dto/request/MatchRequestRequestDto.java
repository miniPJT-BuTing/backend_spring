package com.mini.buting.api.matchRequest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class MatchRequestRequestDto {

    @Builder
    public record CreateMatchRequest(
            @NotNull(message = "대상 팀 ID는 필수입니다.")
            Long targetTeamId
    ) {
    }

    @Builder
    public record RespondMatchRequest(
            @NotNull(message = "응답은 필수입니다.")
            Boolean accept
    ) {
    }
}
