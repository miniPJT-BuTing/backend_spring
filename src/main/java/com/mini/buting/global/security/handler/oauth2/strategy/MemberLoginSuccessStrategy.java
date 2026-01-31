package com.mini.buting.global.security.handler.oauth2.strategy;

import com.mini.buting.global.security.handler.oauth2.OAuth2SuccessHandlerStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <h2>기존 등록 회원 로그인 처리 전략</h2>
 *
 * <p>이미 가입된 소셜 계정 정보가 존재할 경우 실행됨
 * 서버 자체 JWT(Access/Refresh Token)를 생성하고 쿠키에 저장한 뒤, 서비스 메인 페이지로 리다이렉트함.</p>
 */
@Component
public class MemberLoginSuccessStrategy implements OAuth2SuccessHandlerStrategy {
    @Override
    public boolean supports(Authentication authentication, HttpServletRequest request) {
        return false;
    }

    @Override
    public void handle(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }
}
