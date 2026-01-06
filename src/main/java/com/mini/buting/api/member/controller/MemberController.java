package com.mini.buting.api.member.controller;

import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.service.MemberService;
import com.mini.buting.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /**
     * 내 프로필 조회
     * TODO: Spring Security 적용 시 Authentication에서 사용자 정보 추출
     */
    @GetMapping("/profile/me")
    public BaseResponse<MemberProfileResponse> getMyProfile(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        
        log.info("내 프로필 조회 요청: userId={}", userId);
        
        MemberProfileResponse profile = memberService.getMemberProfile(userId);
        
        log.info("내 프로필 조회 성공: userId={}, nickname={}", userId, profile.getNickname());
        return BaseResponse.onSuccess(profile);
    }

    /**
     * 특정 회원 프로필 조회
     */
    @GetMapping("/profile/{memberId}")
    public BaseResponse<MemberProfileResponse> getMemberProfile(@PathVariable Long memberId) {
        
        log.info("회원 프로필 조회 요청: memberId={}", memberId);
        
        MemberProfileResponse profile = memberService.getMemberProfile(memberId);
        
        log.info("회원 프로필 조회 성공: memberId={}, nickname={}", memberId, profile.getNickname());
        return BaseResponse.onSuccess(profile);
    }

    /**
     * 닉네임으로 회원 프로필 조회
     */
//    @GetMapping("/profile/search")
//    public BaseResponse<MemberProfileResponse> getMemberProfileByNickname(
//            @RequestParam String nickname) {
//
//        log.info("닉네임 검색으로 프로필 조회 요청: nickname={}", nickname);
//
//        MemberProfileResponse profile = memberService.getMemberProfileByNickname(nickname);
//
//        log.info("닉네임 검색 프로필 조회 성공: nickname={}", nickname);
//        return BaseResponse.onSuccess(profile);
//    }
}
