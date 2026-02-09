package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.member.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 멤버 정보")
public record ChatMemberResponse(
        @Schema(description = "회원 ID", example = "2")
        Long memberId,

        @Schema(description = "닉네임", example = "김영희")
        String nickname,

        //@Schema(description = "프로필 이미지 URL", nullable = true, example = "https://.../profile.png")
        //String profileImage,

        @Schema(description = "성별", example = "W")
        Gender gender,

        @Schema(description = "팀장 여부", example = "false")
        Boolean isLeader,

        @Schema(description = "대학교명", example = "부산대학교")
        String universityName,

        @Schema(description = "단과대명", example = "경영대학")
        String collegeName,

        @Schema(description = "탈퇴/삭제 여부", example = "false")
        Boolean isDeleted
) {}
