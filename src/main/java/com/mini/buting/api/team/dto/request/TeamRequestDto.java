package com.mini.buting.api.team.dto.request;

import com.mini.buting.api.team.domain.TeamMood;
import com.mini.buting.api.team.domain.TeamSize;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

public class TeamRequestDto {

    @Builder
    public record CreateTeamRequest(
            @NotBlank(message = "팀 제목은 필수입니다.")
            @Size(max = 50, message = "팀 제목은 50자를 초과할 수 없습니다.")
            String title,

            @NotBlank(message = "팀 소개는 필수입니다.")
            @Size(max = 255, message = "팀 소개는 255자를 초과할 수 없습니다.")
            String description,

            @NotNull(message = "선호하는 분위기를 선택해주세요.")
            TeamMood preferredMood,

            @NotNull(message = "팀 크기를 선택해주세요.")
            TeamSize teamSize,

            @NotNull(message = "최소 나이를 입력해주세요.")
            @Min(value = 18, message = "최소 나이는 18세 이상이어야 합니다.")
            @Max(value = 100, message = "최소 나이는 100세 이하여야 합니다.")
            Integer preferredAgeMin,

            @NotNull(message = "최대 나이를 입력해주세요.")
            @Min(value = 18, message = "최대 나이는 18세 이상이어야 합니다.")
            @Max(value = 100, message = "최대 나이는 100세 이하여야 합니다.")
            Integer preferredAgeMax,

            @NotNull(message = "최소 학번을 입력해주세요.")
            @Min(value = 0, message = "최소 학번은 0 이상이어야 합니다.")
            @Max(value = 99, message = "최소 학번은 99 이하여야 합니다.")
            Integer preferredEntryYearMin,

            @NotNull(message = "최대 학번을 입력해주세요.")
            @Min(value = 0, message = "최대 학번은 0 이상이어야 합니다.")
            @Max(value = 99, message = "최대 학번은 99 이하여야 합니다.")
            Integer preferredEntryYearMax,

            @NotEmpty(message = "초대할 멤버를 선택해주세요.")
            List<Long> inviteMemberIds
    ) {
        public CreateTeamRequest {
            // 나이 범위 검증
            if (preferredAgeMin != null && preferredAgeMax != null && preferredAgeMin > preferredAgeMax) {
                throw new IllegalArgumentException("최소 나이는 최대 나이보다 클 수 없습니다.");
            }

            // 학번 범위 검증  
            if (preferredEntryYearMin != null && preferredEntryYearMax != null && preferredEntryYearMin > preferredEntryYearMax) {
                throw new IllegalArgumentException("최소 학번은 최대 학번보다 클 수 없습니다.");
            }

            // 초대 인원 수 검증 (팀 크기 - 1이어야 함)
            if (teamSize != null && inviteMemberIds != null) {
                int expectedInviteCount = teamSize.getSize() - 1;
                if (inviteMemberIds.size() != expectedInviteCount) {
                    throw new IllegalArgumentException(
                            String.format("팀 크기 %s에 맞는 %d명을 초대해야 합니다. (현재: %d명)", 
                                    teamSize.getDisplayName(), expectedInviteCount, inviteMemberIds.size())
                    );
                }
            }
        }
    }

    @Builder
    public record SearchFriendsRequest(
            @Size(max = 50, message = "검색어는 50자를 초과할 수 없습니다.")
            String keyword
    ) {}

    @Builder  
    public record RespondToInvitationRequest(
            @NotNull(message = "응답은 필수입니다.")
            Boolean accept
    ) {}
}
