package com.mini.buting.api.member.dto.request;

import com.mini.buting.api.member.constants.MemberConstants;
import com.mini.buting.api.member.domain.Gender;
import com.mini.buting.api.member.domain.MbtiType;
import com.mini.buting.api.member.domain.PersonalityType;
import com.mini.buting.global.constant.ErrorMessages;
import com.mini.buting.global.constant.Patterns;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * <h2>회원가입 완료 요청</h2>
 * <p>OAuth2 최초 로그인 이후, 서비스 이용에 필요한 추가 프로필 정보를 받아 최종 회원 생성을 완료</p>
 * <hr/>
 * <h5>검증 및 보안 정책</h5>
 * <ol>
 *     <li>{@code signUpToken}을 Key로 Redis에 임시 저장된 소셜 식별 정보(Provider, Email 등)를 조회하여 결합</li>
 *     <li>입력된 {@code universityEmail}은 사전 수행된 인증 기록과 일치해야 하며, {@code universityDomainId}와 매칭되어야 함</li>
 * </ol>
 *
 * @param signUpToken        OAuth2 최초 로그인 시 발급된 회원가입 토큰(UUID)
 * @param nickname           닉네임(2~10자, 한글/영문/숫자)
 * @param universityEmail    인증 완료된 학교 이메일 주소 (Ex: user@pnu.ac.kr)
 * @param universityDomainId 소속 대학 도메인의 고유 식별자
 * @param age                나이(만 17세 ~ 100세 제한)
 * @param gender             성별(MALE, FEMALE)
 * @param mbti               MBTI 유형{@link MbtiType}
 * @param entryYear          학번(2자리, 예: 22)
 * @param personalityTypes   성격 키워드 리스트
 * @param bio                자기소개(선택 항목, 최대 255자, 공백만 입력 불가)
 * @param collegeId          소속 단과대학 고유 식별자
 * @param faceShapeId        추천 알고리즘용 얼굴형 식별자 (선택 항목)
 */
public record SignUpRequest(
        @NotBlank(message = ErrorMessages.SIGN_UP_TOKEN_NOT_FOUND)
        @Pattern(regexp = Patterns.SIGN_UP_TOKEN_REGEX, message = ErrorMessages.INVALID_SIGN_UP_TOKEN)
        String signUpToken,

        @NotBlank(message = ErrorMessages.NICKNAME_NOT_FOUND)
        @Pattern(regexp = Patterns.NICKNAME_REGEX, message = ErrorMessages.INVALID_NICKNAME)
        String nickname,

        @NotBlank(message = ErrorMessages.SCHOOL_EMAIL_NOT_FOUND)
        @Email(message = ErrorMessages.INVALID_EMAIL)
        String universityEmail,

        @NotNull(message = ErrorMessages.UNIVERSITY_DOMAIN_ID_NOT_FOUND)
        @Positive(message = ErrorMessages.INVALID_UNIVERSITY_DOMAIN_ID)
        Long universityDomainId,

        @NotNull(message = ErrorMessages.AGE_NOT_FOUND)
        @Min(value = 17, message = ErrorMessages.INVALID_AGE_RANGE)
        @Max(value = 100, message = ErrorMessages.INVALID_AGE_RANGE)
        Integer age,

        @NotNull(message = ErrorMessages.GENDER_NOT_FOUND)
        Gender gender,

        @NotNull(message = ErrorMessages.MBTI_NOT_FOUND)
        MbtiType mbti,

        @NotNull(message = ErrorMessages.ENTRY_YEAR_NOT_FOUND)
        @Min(value = 0, message = ErrorMessages.INVALID_ENTRY_YEAR)
        @Max(value = 99, message = ErrorMessages.INVALID_ENTRY_YEAR)
        Integer entryYear,

        @NotNull(message = ErrorMessages.PERSONALITY_TYPES_NOT_FOUND)
        @Size(min = 1, max = MemberConstants.Personality.MAX_COUNT, message = ErrorMessages.INVALID_PERSONALITY_TYPES_COUNT)
        List<PersonalityType> personalityTypes,

        @Pattern(regexp = Patterns.BIO_REGEX, message = ErrorMessages.INVALID_BIO)
        String bio,

        @NotNull(message = ErrorMessages.COLLEGE_ID_NOT_FOUND)
        @Positive(message = ErrorMessages.INVALID_COLLEGE_ID)
        Long collegeId,

        @Positive(message = ErrorMessages.INVALID_FACE_SHAPE_ID)
        Long faceShapeId
) {
}
