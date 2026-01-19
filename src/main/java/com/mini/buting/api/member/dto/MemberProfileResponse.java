package com.mini.buting.api.member.dto;

import com.mini.buting.api.member.domain.Gender;
import com.mini.buting.api.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 프로필 조회 응답 DTO
 */
@Getter
@Builder
public class MemberProfileResponse {

    private Long memberId;
    private String nickname;
    private Integer age;
    private Gender gender;
    private String bio;
    
    // 학교 정보
    private String universityName;
    private String collegeName; // 단과대명
    private Integer entryYear; // 학번
    
    // MBTI 정보
    private String mbtiCode; // 예: "ENFP"
    private String mbtiDescription; // MBTI 설명
    
    // 성격 키워드 3개
    private List<PersonalityInfo> personalities;
    
    // AI 얼굴형 분석 결과
    private String faceShape;

    /**
     * 성격 키워드 정보
     */
    @Getter
    @Builder
    public static class PersonalityInfo {
        private String code; // 예: "EXTROVERTED"
        private String description; // 예: "외향적"
    }

    /**
     * Member 엔티티로부터 DTO 생성
     */
    public static MemberProfileResponse from(Member member) {
        if (member == null) {
            return null;
        }

        // 성격 키워드 정보 변환
        List<PersonalityInfo> personalityInfos = member.getPersonalityTypes().stream()
                .map(personalityType -> PersonalityInfo.builder()
                        .code(personalityType.getCode())
                        .description(personalityType.getDescription())
                        .build())
                .toList();

        return MemberProfileResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .age(member.getAge())
                .gender(member.getGender())
                .bio(member.getBio())
                .universityName(member.getUniversity() != null ? member.getUniversity().getName() : null)
                .collegeName(member.getCollege() != null ? member.getCollege().getName() : null)
                .entryYear(member.getEntryYear())
                .mbtiCode(member.getMbtiCode())
                .mbtiDescription(member.getMbti() != null ? member.getMbti().getDescription() : null)
                .personalities(personalityInfos)
                .faceShape(member.getFaceShapeName())
                .build();
    }
}
