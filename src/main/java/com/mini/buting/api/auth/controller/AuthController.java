package com.mini.buting.api.auth.controller;

import com.mini.buting.api.auth.service.AuthTokenService;
import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.security.constant.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "사용자 인증/인가 관련 API")
public class AuthController {
    private final AuthTokenService authTokenService;

    @Operation(summary = "JWT 재발급 API", description = "RT로 만료된 AT를 새로 발급")
    @PostMapping("/reissue")
    public BaseResponse<Void> refresh(
            @CookieValue(value = SecurityConstants.Token.REFRESH_COOKIE_NAME) String refreshToken,
            HttpServletResponse response) {
        authTokenService.refreshToken(response, refreshToken);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "로그아웃 API", description = "기존에 사용하던 AT/RT를 무효화")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authTokenService.logout(request, response);
        return BaseResponse.onSuccess();
    }

}
