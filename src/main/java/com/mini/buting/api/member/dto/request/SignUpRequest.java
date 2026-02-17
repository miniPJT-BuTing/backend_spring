package com.mini.buting.api.member.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mini.buting.api.member.domain.Gender;
import com.mini.buting.api.member.domain.MbtiType;
import com.mini.buting.global.constant.ErrorMessages;
import com.mini.buting.global.constant.Patterns;
import jakarta.validation.constraints.*;

/**
 * <h2>회원가입 완료 요청</h2>
 * <p>OAuth2 최초 로그인 이후, 추가 프로필 입력값만 받아 회원 생성을 완료</p>
 * <hr/>
 * <h5>기타 참고</h5>
 * <ol>
 *     <li>소셜 식별 정보(provider/providerId/email)은 요청 본문이 아닌 signUpToken(Redis payload)에서 복원</li>
 *     <li>Member 필수 컬럼을 누락 없이 입력받도록 검증하고 있음</li>
 * </ol>
 *
 * @param signUpToken     OAuth2 최초 로그인 시 발급된 회원가입 토큰(UUID)
 * @param nickname        닉네임(2~10자, 한글/영문/숫자)
 * @param universityEmail 학교 이메일 전체 주소(예: user@pnu.ac.kr)
 * @param age             나이
 * @param gender          성별
 * @param mbti            MBTI 유형
 * @param entryYear       학번(2자리, 예: 22)
 * @param bio             자기소개(선택, 공백만 입력 불가, 최대 255자)
 * @param collegeId       단과대 ID(기존 majorId 요청도 허용)
 * @param faceShapeId     얼굴형 ID(선택)
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

        @NotNull(message = ErrorMessages.AGE_NOT_FOUND)
        @Min(value = 17, message = ErrorMessages.INVALID_AGE_RANGE)
        @Max(value = 100, message = ErrorMessages.INVALID_AGE_RANGE)
        Integer age,

        @NotBlank(message = ErrorMessages.GENDER_NOT_FOUND)
        Gender gender,

        @NotBlank(message = ErrorMessages.MBTI_NOT_FOUND)
        MbtiType mbti,

        @NotNull(message = ErrorMessages.ENTRY_YEAR_NOT_FOUND)
        @Min(value = 0, message = ErrorMessages.INVALID_ENTRY_YEAR)
        @Max(value = 99, message = ErrorMessages.INVALID_ENTRY_YEAR)
        Integer entryYear,

        @Pattern(regexp = Patterns.BIO_REGEX, message = ErrorMessages.INVALID_BIO)
        String bio,

        @NotNull(message = ErrorMessages.COLLEGE_ID_NOT_FOUND)
        @Positive(message = ErrorMessages.INVALID_COLLEGE_ID)
        @JsonAlias("majorId")
        Long collegeId,

        @Positive(message = ErrorMessages.INVALID_FACE_SHAPE_ID)
        Long faceShapeId
) {
}
