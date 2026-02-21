package com.mini.buting.api.member.service;

import com.mini.buting.api.auth.dto.response.MemberAvailabilityResponse;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.api.member.repository.MemberSocialRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;

    /**
     * 회원 프로필 조회
     *
     * @param memberId 조회할 회원 ID
     * @return 회원 프로필 정보
     */
    public MemberProfileResponse getMemberProfile(Long memberId) {
        log.debug("회원 프로필 조회 요청: memberId={}", memberId);

        // 1. 회원 조회
        Member member = memberRepository.findByIdWithDetails(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        // 2. 탈퇴한 사용자 체크
        if (member.getIsDeleted()) {
            log.warn("탈퇴한 사용자의 프로필 조회 시도: memberId={}", memberId);
            throw new BaseException(BaseResponseStatus.MEMBER_DELETED_USER);
        }

        // 3. DTO 변환 후 반환
        MemberProfileResponse response = MemberProfileResponse.from(member);
        log.debug("회원 프로필 조회 성공: memberId={}, nickname={}", memberId, member.getNickname());

        return response;
    }

    /**
     * 닉네임으로 회원 조회 (검색 기능용)
     *
     * @param nickname 닉네임
     * @return 회원 프로필 정보
     */
    public MemberProfileResponse getMemberProfileByNickname(String nickname) {
        log.debug("닉네임으로 회원 프로필 조회 요청: nickname={}", nickname);

        Member member = memberRepository.findByNicknameWithDetails(nickname)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        if (member.getIsDeleted()) {
            throw new BaseException(BaseResponseStatus.MEMBER_DELETED_USER);
        }

        return MemberProfileResponse.from(member);
    }

    /**
     * <h2>가용성(중복) 체크</h2>
     * <p>이메일이 들어오면 소셜 이메일 중복을, 닉네임이 들어오면 닉네임 중복을 확인</p>
     *
     * @param email    가용성을 확인할 이메일 (Optional)
     * @param nickname 가용성을 확인할 닉네임 (Optional)
     * @return BaseException 파라미터가 모두 누락된 경우 {@code INVALID_REQUEST} 발생
     */
    public List<MemberAvailabilityResponse> checkAvailability(String email, String nickname) {
        boolean hasEmail = StringUtils.hasText(email);
        boolean hasNickname = StringUtils.hasText(nickname);
        if (!hasEmail && !hasNickname) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        List<MemberAvailabilityResponse> responses = new ArrayList<>();

        if (hasEmail) {
            boolean isDuplicated = memberSocialRepository.existsByEmailAndMember_IsDeletedFalse(email);
            responses.add(MemberAvailabilityResponse.of(MemberAvailabilityResponse.AvailabilityType.EMAIL, email, !isDuplicated));
        }

        if (hasNickname) {
            boolean isDuplicated = memberRepository.existsByNicknameAndIsDeletedFalse(nickname);
            responses.add(MemberAvailabilityResponse.of(MemberAvailabilityResponse.AvailabilityType.NICKNAME, nickname, !isDuplicated));
        }

        return responses;
    }
}
