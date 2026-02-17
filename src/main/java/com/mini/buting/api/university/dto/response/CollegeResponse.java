package com.mini.buting.api.university.dto.response;

import com.mini.buting.api.university.domain.College;

/**
 * <h2>단과대 목록 응답 DTO</h2>
 *
 * @param id   단과대 ID
 * @param name 단과대명
 */
public record CollegeResponse(
        Long id,
        String name
) {
    /**
     * College Entity를 응답 DTO로 변환
     */
    public static CollegeResponse from(College college) {
        return new CollegeResponse(college.getId(), college.getName());
    }
}
