package com.mini.buting.global.security.handler.oauth2.strategy;

import com.mini.buting.global.security.handler.oauth2.OAuth2SuccessHandlerStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <h2>최초 소셜 로그인(Guest) 가입 처리 전략</h2>
 *
 * <p>서비스에 처음 방문한 소셜 계정일 경우 실행됨.
 * 가입되지 않은 상태임로 추가 정보 입력을 위한 회원가입 API로 리다이렉트</p>
 */
@Component
public class GuestSignUpSuccessStrategy implements OAuth2SuccessHandlerStrategy {
    @Override
    public boolean supports(Authentication authentication, HttpServletRequest request) {
        return false;
    }

    @Override
    public void handle(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }
}
