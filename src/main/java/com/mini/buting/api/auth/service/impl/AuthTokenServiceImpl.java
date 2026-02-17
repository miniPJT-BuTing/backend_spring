package com.mini.buting.api.auth.service.impl;

import com.mini.buting.api.auth.service.AuthTokenService;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.security.provider.JwtTokenProvider;
import com.mini.buting.global.security.service.RefreshTokenStore;
import com.mini.buting.global.security.service.TokenBlacklistService;
import com.mini.buting.global.security.token.JwtToken;
import com.mini.buting.global.security.token.TokenIdentity;
import com.mini.buting.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthTokenServiceImpl implements AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenBlacklistService tokenBlacklistService;
    private final CookieUtils cookieUtils;
    private final SecurityProperties securityProperties;
    private final MemberRepository memberRepository;

    /**
     * <h3>세션 단위 로그아웃</h3>
     * <p>AccessToken에서 {@code memberUuid(sub)} 및 {@code session_uuid}를 추출하여,
     * 해당 세션의 RefreshToken을 Redis에서 삭제하고 AccessToken을 블랙리스트에 등록한 뒤,
     * RefreshToken의 쿠키를 만료 처리함.</p>
     *
     * @param request  Authorization 헤더에서 AccessToken 추출에 사용
     * @param response RefreshToken 쿠키 만료에 사용
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.getTokenFromRequest(request);
        if (!StringUtils.hasText(accessToken)) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_REQUIRED);
        }

        TokenIdentity identity = jwtTokenProvider.extractTokenIdentityAllowedExpired(accessToken);

        refreshTokenStore.delete(identity.subject(), identity.sessionUuid());
        tokenBlacklistService.register(accessToken);
        cookieUtils.expireCookie(response, SecurityConstants.Token.REFRESH_COOKIE_NAME);
    }

    @Override
    public void issueJwt(AuthUser authUser, HttpServletResponse response) {
        String memberUuid = authUser.getUuid();
        String authorities = authUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        JwtToken jwtToken = issueAndStoreTokenSet(memberUuid, authorities);
        // response.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.Token.GRANT_TYPE + jwtToken.accessToken());
        writeRefreshCookie(response, jwtToken.refreshToken());
    }

    /**
     * <h3>RefreshToken 기반 AccessToken 재발급</h3>
     * <ol>
     *     <li>RT 서명/만료/클레임 검증</li>
     *     <li>Redis 저장 RT와 일치 여부 검증(서버 발급 토큰인지 확인)</li>
     *     <li>성공 시 RT 회전(rotation) 및 새 AT/RT 발급</li>
     * </ol>
     *
     * @param response     Authorization 헤더와 RT 쿠키 갱신에 사용
     * @param refreshToken 쿠키에서 전달된 RefreshToken
     */
    @Override
    public void refreshToken(HttpServletResponse response, String refreshToken) {
        // RT 서명/만료/클레임 검증
        if (!StringUtils.hasText(refreshToken)) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_REQUIRED);
        }

        TokenIdentity identity = jwtTokenProvider.extractTokenIdentity(refreshToken);
        String saved = refreshTokenStore.get(identity.subject(), identity.sessionUuid())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN));

        if (!refreshToken.equals(saved)) {
            refreshTokenStore.delete(identity.subject(), identity.sessionUuid());
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        }

        var member = memberRepository.findByUuid(identity.subject())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        if (Boolean.TRUE.equals(member.getIsDeleted())) {
            refreshTokenStore.delete(identity.subject(), identity.sessionUuid());
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        }

        refreshTokenStore.delete(identity.subject(), identity.sessionUuid());

        JwtToken reissued = issueAndStoreTokenSet(member.getUuid(), member.getRole().getName());
        writeAccessHeader(response, reissued.accessToken());
        writeRefreshCookie(response, reissued.refreshToken());
    }

    private JwtToken issueAndStoreTokenSet(String memberUuid, String authorities) {
        JwtToken jwtToken = jwtTokenProvider.generateTokenSet(memberUuid, authorities);
        TokenIdentity refreshIdentity = jwtTokenProvider.extractTokenIdentity(jwtToken.refreshToken());

        Duration refreshTtl = securityProperties.jwt().expireTime().refresh();
        refreshTokenStore.save(
                refreshIdentity.subject(),
                refreshIdentity.sessionUuid(),
                jwtToken.refreshToken(),
                refreshTtl
        );

        return jwtToken;
    }

    private void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
        cookieUtils.setCookie(
                response,
                SecurityConstants.Token.REFRESH_COOKIE_NAME,
                refreshToken,
                (int) securityProperties.jwt().expireTime().refresh().getSeconds()
        );
    }

    private void writeAccessHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.Token.GRANT_TYPE + accessToken);
    }
}
