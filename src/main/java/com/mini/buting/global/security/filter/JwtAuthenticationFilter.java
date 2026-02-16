package com.mini.buting.global.security.filter;

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
 * <p>모든 요청에 대해 실행되며, HTTP 헤더의 AccessToken을 확인하고, 토큰이 존재하는 경우에만 인증을 시도함.</p>
 * <p>이 필터는 요청을 '차단'하지 않음. 토큰이 없거나 유효하지 않으면 인증 객체를 생성하지 않고 다음 체인으로 넘어감.</p>
 * <p>
 * <hr/>
 * <h5>역할</h5>
 * <ol>
 *     <li>AccessToken 추출</li>
 *     <li>블랙리스트 여부 확인</li>
 *     <li>토큰 검증 및 Authentication 생성</li>
 *     <li>성공 시 {@link org.springframework.security.core.context.SecurityContext}에 인증 객체 저장</li>
 * </ol>
 * <p>
 * <hr/>
 * <h5>[참고] 인가(Authorization) 처리 흐름</h5>
 * <ul>
 *     <li>경로별 접근 허용/차단(permitAll/authenticated/hasRole)은 {@code SecurityConfig.authorizeHttpRequests(...)}에서 정의되며,</li>
 *     <li>실제 집행은 Spring Security 내부 {@code AuthorizationFilter} 및 {@code AuthenticationEntryPoint/AccessDeniedHandler}가 담당</li>
 * </ul>
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
        // Request Header에서 토큰 추출
        String token = tokenProvider.getTokenFromRequest(request);

        // 토큰이 없으면 인증 객체를 만들지 않고 진행(인가 판단은 AuthorizationFilter가 수행)
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 블랙리스트/유효성 검증 실패 시 인증 포기(차단은 인가 단계에서 처리)
            if (!tokenProvider.validateToken(token) || tokenBlacklistService.isBlacklisted(token)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
