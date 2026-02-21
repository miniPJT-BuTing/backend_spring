package com.mini.buting.api.auth.service;

import com.mini.buting.api.analysis.domain.FaceShape;
import com.mini.buting.api.analysis.repository.FaceShapeRepository;
import com.mini.buting.api.auth.dto.OAuth2SignUpPayload;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.domain.MemberSocial;
import com.mini.buting.api.member.domain.SocialProvider;
import com.mini.buting.api.member.dto.request.SignUpRequest;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.api.member.repository.MemberSocialRepository;
import com.mini.buting.api.university.domain.College;
import com.mini.buting.api.university.domain.UniversityDomain;
import com.mini.buting.api.university.repository.CollegeRepository;
import com.mini.buting.api.university.repository.UniversityDomainRepository;
import com.mini.buting.api.university.service.UniversityDomainQueryService;
import com.mini.buting.api.university.util.UniversityEmailParser;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.mail.constants.MailConstants;
import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * <h2>회원가입 서비스</h2>
 * <p>소셜 임시토큰 + 학교 이메일 인증 결과를 조합해 Member/MemberSocial 생성</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SignUpService {
    private static final int MAX_UUID_RETRY = 5;

    private final RedisUtils redisUtils;
    private final UniversityDomainQueryService universityDomainQueryService;
    private final UniversityDomainRepository universityDomainRepository;
    private final UniversityEmailParser universityEmailParser;
    private final CollegeRepository collegeRepository;
    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;
    private final FaceShapeRepository faceShapeRepository;

    public void signUp(SignUpRequest requestDto) {
        String signUpKey = SecurityConstants.Redis.OAUTH2_SIGNUP_PREFIX + requestDto.signUpToken();
        OAuth2SignUpPayload payload = getSignUpPayloadOrThrow(signUpKey);
        payload.validateRequiredOrThrow();

        String verifiedKey = MailConstants.Redis.verifiedKey(
                universityEmailParser.normalize(requestDto.universityEmail()),
                MailType.SIGN_UP
        );
        if (!redisUtils.isKeyExist(verifiedKey)) {
            throw new BaseException(BaseResponseStatus.MAIL_VERIFICATION_REQUIRED);
        }

        validateUniversityDomainMatch(requestDto.universityEmail(), requestDto.universityDomainId());
        validateDuplicates(requestDto.nickname(), payload.provider(), payload.providerId(), requestDto.universityEmail());

        UniversityDomain universityDomain = universityDomainRepository.findById(requestDto.universityDomainId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL));

        College college = collegeRepository.findById(requestDto.collegeId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COLLEGE_NOT_FOUND));

        FaceShape faceShape = null;
        if (requestDto.faceShapeId() != null) {
            faceShape = faceShapeRepository.findById(requestDto.faceShapeId()).orElse(null);
        }

        String localPart = universityEmailParser.extractLocalPart(requestDto.universityEmail());
        String uuid = generateUniqueMemberUuid();

        Member member = Member.of(requestDto, universityDomain, college, faceShape, uuid, localPart);
        Member saved = memberRepository.save(member);
        saved.setPersonalities(requestDto.personalityTypes());

        memberSocialRepository.save(MemberSocial.of(saved, payload));

        redisUtils.deleteValue(signUpKey);
        redisUtils.deleteValue(verifiedKey);
    }

    private OAuth2SignUpPayload getSignUpPayloadOrThrow(String key) {
        return redisUtils.getValue(key, OAuth2SignUpPayload.class)
                .orElseGet(() -> {
                    Object rawMap = redisUtils.getValue(key);
                    return OAuth2SignUpPayload.from(rawMap);
                });
    }

    private void validateUniversityDomainMatch(String email, Long universityDomainId) {
        Long resolvedId = universityDomainQueryService.resolveByEmail(email).universityDomainId();
        if (!resolvedId.equals(universityDomainId)) {
            throw new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL);
        }
    }

    private void validateDuplicates(String nickname, String providerName, String providerId, String fullEmail) {
        if (memberRepository.existsByNicknameAndIsDeletedFalse(nickname)) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        SocialProvider provider = SocialProvider.from(providerName);
        if (memberSocialRepository.findByProviderNameAndProviderId(provider, providerId).isPresent()) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        String localPart = universityEmailParser.extractLocalPart(fullEmail);
        String domain = universityEmailParser.extractDomain(fullEmail);
        if (memberRepository.findByUniversityEmail(localPart, domain).isPresent()) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
    }

    private String generateUniqueMemberUuid() {
        for (int i = 0; i < MAX_UUID_RETRY; i++) {
            String candidate = UUID.randomUUID().toString();
            if (memberRepository.findByUuid(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
