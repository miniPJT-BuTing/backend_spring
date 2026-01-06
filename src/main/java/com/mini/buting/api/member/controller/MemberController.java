package com.mini.buting.api.member.controller;

import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.service.MemberService;
import com.mini.buting.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1") // v1 버전 prefix 추가, /api 제거
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /**
     * 내 프로필 조회
     * TODO: Spring Security 적용 시 Authentication에서 사용자 정보 추출
     */
    @GetMapping("/me/profile")  // /v1/me/profile
    public BaseResponse<MemberProfileResponse> getMyProfile(
                    @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {

        log.debug("내 프로필 조회 요청: userId={}", userId);

        MemberProfileResponse profile = memberService.getMemberProfile(userId);

        log.debug("내 프로필 조회 성공: userId={}, nickname={}", userId, profile.getNickname());
        return BaseResponse.onSuccess(profile);
    }

    /**
     * 특정 회원 프로필 조회
     */
    @GetMapping("/users/{userId}/profile") // /v1/users/{userId}/profile
    public BaseResponse<MemberProfileResponse> getMemberProfile(@PathVariable Long userId) {

        log.debug("회원 프로필 조회 요청: userId={}", userId);

        MemberProfileResponse profile = memberService.getMemberProfile(userId);

        log.debug("회원 프로필 조회 성공: userId={}, nickname={}", userId, profile.getNickname());
        return BaseResponse.onSuccess(profile);
    }

    /**
     * 닉네임으로 회원 프로필 조회 (추후 구현 예정)
     */
    //    @GetMapping("/users/search")
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
