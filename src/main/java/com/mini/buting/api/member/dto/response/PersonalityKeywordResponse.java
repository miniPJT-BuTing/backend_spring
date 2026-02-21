package com.mini.buting.api.member.dto.response;

import com.mini.buting.api.member.domain.PersonalityType;

public record PersonalityKeywordResponse(
        String code,
        String description
) {
    public static PersonalityKeywordResponse from(PersonalityType type) {
        return new PersonalityKeywordResponse(type.getCode(), type.getDescription());
    }
}
