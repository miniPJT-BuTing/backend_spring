package com.mini.buting.api.member.controller;

import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.dto.SignUpRequestDto;
import com.mini.buting.api.member.service.MemberService;
import com.mini.buting.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
@Tag(name = "Member", description = "사용자 관련 API")
public class MemberController {

    private final MemberService memberService;

    /**
     * 내 프로필 조회
     * TODO: Spring Security 적용 시 Authentication에서 사용자 정보 추출
     */
    @GetMapping("/profile/me")
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
    @GetMapping("/profile/{userId}")
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

    /**
     * @implNote 소셜 로그인 이후 진행되는 API라, 소셜 정보 연계가 필요함.
     */
    @Operation(summary = "회원가입 API", description = "신규 사용자를 등록")
    @PostMapping()
    public BaseResponse<Void> signUp(@Valid @RequestBody SignUpRequestDto requestDto, HttpServletResponse response) {
        // TODO: 회원가입 서비스 로직 호출
        return BaseResponse.onSuccess();
    }
}
