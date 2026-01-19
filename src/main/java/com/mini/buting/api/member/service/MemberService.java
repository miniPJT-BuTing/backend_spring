package com.mini.buting.api.member.service;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 프로필 조회
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
}
