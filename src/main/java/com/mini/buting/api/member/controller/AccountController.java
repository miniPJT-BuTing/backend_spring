package com.mini.buting.api.member.controller;

import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.dto.request.UpdateMyProfileRequest;
import com.mini.buting.api.member.service.MemberService;
import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.util.AuthValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members/me")
@Tag(name = "Member - Account", description = "사용자 계정/마이페이지 관련 API")
public class AccountController {

    private final MemberService memberService;

    @GetMapping()
    public BaseResponse<MemberProfileResponse> getMyProfile(@AuthenticationPrincipal AuthUser authUser) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("내 프로필 조회 요청: userId={}", memberId);

        MemberProfileResponse profile = memberService.getMemberProfile(memberId);
        log.debug("내 프로필 조회 성공: userId={}, nickname={}", memberId, profile.getNickname());

        return BaseResponse.onSuccess(profile);
    }

    @Operation(summary = "회원탈퇴 API", description = "기존 사용자를 비활성화")
    @DeleteMapping()
    public BaseResponse<Void> withDraw(
            HttpServletRequest request, HttpServletResponse response,
            @AuthenticationPrincipal AuthUser authUser) {
        long memberId = AuthValidator.require(authUser).getId();
        // TODO: 필요한 연계 엔티티 초기화
        // TODO: 회원 삭제
        // TODO: 로그아웃 처리
        return BaseResponse.onSuccess();
    }
    @Operation(summary = "내 프로필 수정", description = "로그인한 사용자의 프로필(닉네임/MBTI/성격키워드/자기소개)을 수정합니다.")
    @PatchMapping()
    public BaseResponse<MemberProfileResponse> updateMyProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UpdateMyProfileRequest request) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("내 프로필 수정 요청: memberId={}", memberId);

        MemberProfileResponse response = memberService.updateMyProfile(memberId, request);
        return BaseResponse.onSuccess(response);
    }
}
