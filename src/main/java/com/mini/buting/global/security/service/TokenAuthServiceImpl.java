package com.mini.buting.global.security.service;

import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.security.provider.JwtTokenProvider;
import com.mini.buting.global.security.token.JwtToken;
import com.mini.buting.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenAuthServiceImpl implements TokenAuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;
    private final SecurityProperties securityProperties;

    @Override
    public void issueJwt(AuthUser authUser, HttpServletResponse response) {
        String memberUuid = authUser.getUuid();
        String authorities = authUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        JwtToken jwtToken = jwtTokenProvider.generateTokenSet(memberUuid, authorities);

        // response.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.Token.GRANT_TYPE + jwtToken.accessToken());

        cookieUtils.setCookie(
                response,
                SecurityConstants.Token.REFRESH_COOKIE_NAME,
                jwtToken.refreshToken(),
                (int) securityProperties.jwt().expireTime().refresh().getSeconds()
        );
    }
}
