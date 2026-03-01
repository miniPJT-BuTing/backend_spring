package com.mini.buting.api.member.dto.request;

import com.mini.buting.api.member.constants.MemberConstants;
import com.mini.buting.api.member.domain.MbtiType;
import com.mini.buting.api.member.domain.PersonalityType;
import com.mini.buting.global.constant.ErrorMessages;
import com.mini.buting.global.constant.Patterns;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 내 프로필 수정 요청 DTO (PATCH)
 * - 부분 수정 용도로 모든 필드는 optional
 * - 최소 1개 이상 필드가 포함되어야 하는 검증은 서비스에서 수행
 */
public record UpdateMyProfileRequest(
        @Pattern(regexp = Patterns.NICKNAME_REGEX, message = ErrorMessages.INVALID_NICKNAME)
        String nickname,

        MbtiType mbti,

        @Size(min = 1, max = MemberConstants.Personality.MAX_COUNT, message = ErrorMessages.INVALID_PERSONALITY_TYPES_COUNT)
        List<PersonalityType> personalityTypes,

        @Pattern(regexp = Patterns.BIO_REGEX, message = ErrorMessages.INVALID_BIO)
        String bio
) {
}
