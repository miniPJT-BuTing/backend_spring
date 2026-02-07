package com.mini.buting.global.security.provider;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.security.token.JwtToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * <h2>JWT Token Provider</h2>
 * <p>Security 인증을 위한 JWT의 발급, 파싱 및 유효성 검증을 담당</p>
 */
@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final SecurityProperties securityProperties;

    public JwtTokenProvider(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        // Base64 인코딩된 SecretKey를 디코딩하여 HMAC-SHA 키 생성
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(securityProperties.jwt().secretKey()));
    }

    /**
     * 사용자의 식별값과 권한 정보를 바탕으로 Access & Refresh Token 세트 생성
     * <p>Refresh Token의 경우 권한 클레임을 생략</p>
     *
     * @param subject     토큰의 주체
     * @param authorities 쉼표(,)로 구분된 사용자 권한 목록(Ex: "ROLE_USER,ROLE_ADMIN")
     * @return 발급된 {@link JwtToken} 객체 (Grant Type, Access/Refresh Token 포함)
     */
    public JwtToken generateTokenSet(String subject, String authorities) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);

        // AccessToken 생성
        Date accessExpirationDate = new Date(now + securityProperties.jwt().expireTime().access().toMillis());
        String accessToken = generateToken(subject, authorities, issuedAt, accessExpirationDate);

        // RefreshToken 생성
        Date refreshExpirationDate = new Date(now + securityProperties.jwt().expireTime().refresh().toMillis());
        String refreshToken = generateToken(subject, null, issuedAt, refreshExpirationDate);

        return JwtToken.of(SecurityConstants.Token.GRANT_TYPE.trim(), accessToken, refreshToken);
    }

    /**
     * 실제 JWT를 생성하는 내부 메서드
     *
     * @param subject     토큰 주체
     * @param authorities 권한 클레임 ({@code null} 일 경우 제외)
     * @param issuedAt    발급 시각
     * @param expiration  만료 시각
     * @return 생성된 JWT String
     */
    private String generateToken(String subject, String authorities, Date issuedAt, Date expiration) {
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256);

        if (StringUtils.hasText(authorities)) {
            builder.claim(SecurityConstants.Token.AUTHORITIES_CLAIM, authorities);
        }

        return builder.compact();
    }

    /**
     * 전달된 토큰을 복화하여 내부 클레임(Payload)을 반환
     *
     * @param token 검증 및 파싱 대상 JWT
     * @return 파싱된 {@link Claims} 객체
     * @throws BaseException 토큰이 만료되었거나 서명이 유효하지 않은 경우 발생
     * @implNote TODO: 내부 log 출력을 AOP로 분리하고, 이후 디버깅 최적화를 위해 MCP를 적용할 것
     */
    public Claims parseClaims(String token) {
        try {
            return getClaims(token);
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
            log.warn("{} Invalid JWT signature or format: {}", SecurityConstants.Log.LOG_PREFIX, e.getMessage());
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            log.warn("{} JWT token is expired: {}", SecurityConstants.Log.LOG_PREFIX, e.getMessage());
            throw new BaseException(BaseResponseStatus.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("{} JWT token is unsupported: {}", SecurityConstants.Log.LOG_PREFIX, e.getMessage());
            throw new BaseException(BaseResponseStatus.UNSUPPORTED_JWT_TOKEN);
        }
    }

    /**
     * 토큰의 유효성을 boolean 값으로 반환
     * <p>필터 계층에서 인증 여부 판단을 위해 호출됨</p>
     * @implNote TODO: 필터 계층에서 내부 헬퍼 메소드로 옮겨도 될 듯 함.
     *
     * @param token 검증할 JWT
     * @return 유효할 경우 {@code true}, 그렇지 않다면 {@code false}
     * @see com.mini.buting.global.security.filter.JwtAuthenticationFilter
     */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            parseClaims(token);
            return true;
        } catch (BaseException e) {
            return false;
        }
    }

    /**
     * 토큰에서 subject 정보를 추출
     * <p>토큰 재발급을 위해 만료된 토큰에서도 서명(sub)만 유효하다면 동일하게 claims 정보를 파싱합니다.</p>
     *
     * @param token 정보를 추출할 JWT 토큰
     * @return 토큰의 subject 정보
     */
    public String getSubjectFromToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_REQUIRED);
        }

        try {
            return getClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("{} Failed to extract subject: {}", SecurityConstants.Log.LOG_PREFIX, e.getMessage());
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        }
    }

    /**
     * 토큰에서 클레임 정보를 파싱하기 위한 내부 헬퍼 메서드
     * <p>서명 검증을 포함한 순수 JWT 파싱 로직을 담당
     * 예외의 경우 직접 처리하지 않고, 호출자에게 던져 세부 처리를 위임함.</p>
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
