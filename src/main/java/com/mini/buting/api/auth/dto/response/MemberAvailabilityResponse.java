package com.mini.buting.api.auth.dto.response;

import lombok.Builder;

/**
 * <h2>회원가입 정보 중복 확인 응답 DTO</h2>
 * <p>이메일, 닉네임 등 회원 가입 시 필요한 정보의 중복 여부 및 가용성을 클라이언트에게 전달,
 * 특정 값에 대한 명확한 식별을 위해 체크한 타입과 해당 값을 함께 반환함.</p>
 *
 * @param type        가용성 체크를 수행한 항목의 종류(NICKNAME, EMAIL 등)
 * @param value       체크를 수행한 실제 입력 값(Request)
 * @param isAvailable 해당 값의 사용 여부(true: 사용 가능, false: 중복 또는 사용)
 */
public record MemberAvailabilityResponse(
        AvailabilityType type,
        String value,
        boolean isAvailable
) {

    @Builder
    public MemberAvailabilityResponse(AvailabilityType type, String value, boolean isAvailable) {
        this.type = type;
        this.value = value;
        this.isAvailable = isAvailable;
    }

    public static MemberAvailabilityResponse of(AvailabilityType type, String value, boolean isAvailable) {
        return MemberAvailabilityResponse.builder()
                .type(type)
                .value(value)
                .isAvailable(isAvailable)
                .build();
    }

    public enum AvailabilityType {
        NICKNAME,
        EMAIL
    }
}
