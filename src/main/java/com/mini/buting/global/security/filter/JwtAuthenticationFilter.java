package com.mini.buting.global.security.filter;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.security.provider.JwtTokenProvider;
import com.mini.buting.global.security.service.TokenBlacklistService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <h2>JWT 인증 필터</h2>
 * <p>모든 요청에 대해 실행되며, HTTP 헤더의 JWT 토큰을 검증하여 SecurityContext에 인증 객체를 저장</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final SecurityProperties securityProperties;
    private final TokenBlacklistService tokenBlacklistService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 현재 요청이 화이트리스트(인증 불필요)에 해당하는지 확인
        boolean isWhitelist = isPermitAll(method, uri);

        // Request Header에서 토큰 추출
        String token = tokenProvider.getTokenFromRequest(request);

        // 토큰이 없는 경우, 화이트 리스트면 통과. 아니면 이후 FilterSecurityInterceptor에서 거부됨
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 유효성 및 블랙리스트 여부 검증
            if (tokenProvider.validateToken(token) && !tokenBlacklistService.isBlacklisted(token)) {
                // 인증 성공 -> 컨텍스트에 사용자 정보 저장
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // 인증 실패: 화이트리스트가 아니면 예외 발생, 맞으면 컨텍스트 클리어 후 익명 사용자 진행
                handleInvalidToken(isWhitelist);
            }
        } catch (BaseException e) {
            handleException(e, isWhitelist);
        } catch (Exception e) {
            handleException(new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN), isWhitelist);
        }
    }

    /**
     * 유효하지 않은 토큰에 대한 공통 처리
     * <p>화이트리스트가 아니면 예외 발생, 맞으면 컨텍스트 클리어 후 익명 사용자 진행</p>
     */
    private void handleInvalidToken(boolean isWhitelist) {
        if (!isWhitelist) {
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * 예외 발생 시 화이트리스트 여부에 따른 분기 처리
     */
    private void handleException(Exception e, boolean isWhitelist) {
        if (!isWhitelist) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * WhitelistProperties 기반 AntPath 맵핑 검사
     */
    private boolean isPermitAll(String method, String uri) {
        return securityProperties.whitelist().values().entrySet().stream()
                .anyMatch(entry -> {
                    if (entry.getKey().equalsIgnoreCase(method)) {
                        return entry.getValue().stream()
                                .anyMatch(pattern -> antPathMatcher.match(pattern, uri));
                    }
                    return false;
                });
    }
}
