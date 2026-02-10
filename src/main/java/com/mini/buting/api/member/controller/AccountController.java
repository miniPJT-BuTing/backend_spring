package com.mini.buting.api.member.controller;

import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.security.principal.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members/me")
@Tag(name = "Member - Account", description = "사용자 계정/마이페이지 관련 API")
public class AccountController {
    // TODO: 기존 마이페이지 API 이쪽으로 다 옮기기

    @Operation(summary = "회원탈퇴 API", description = "기존 사용자를 비활성화")
    @DeleteMapping("/me")
    public BaseResponse<Void> withDraw(
            HttpServletRequest request, HttpServletResponse response,
            @AuthenticationPrincipal AuthUser authUser) {
        long memberId = authUser.getId();
        // TODO: 필요한 연계 엔티티 초기화
        // TODO: 회원 삭제
        // TODO: 로그아웃 처리
        return BaseResponse.onSuccess();
    }
}
