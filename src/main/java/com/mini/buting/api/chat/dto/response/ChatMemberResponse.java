package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.member.domain.Gender;

public record ChatMemberResponse(
        Long memberId,
        String nickname,
        Gender gender,
        Boolean isLeader,
        String universityName,
        String collegeName
) {
}
