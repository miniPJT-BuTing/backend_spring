package com.mini.buting.global.security.provider;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.security.token.JwtToken;
import com.mini.buting.global.security.token.TokenIdentity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * <h2>JWT 생성/파싱 전용 Provider</h2>
 *
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
     * <p>Refresh Token의 경우 권한 클레임을 생략하며,
     * 로그인 세션마다 고유한 {@code session_uuid}를 생성해 토큰 클레임에 포함함.</p>
     *
     * @param subject     토큰의 주체
     * @param authorities 쉼표(,)로 구분된 사용자 권한 목록(Ex: "ROLE_USER,ROLE_ADMIN")
     * @return 발급된 {@link JwtToken} 객체 (Grant Type, Access/Refresh Token 포함)
     */
    public JwtToken generateTokenSet(String subject, String authorities) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);

        // 이번 로그인을 위한 고유한 세션 ID 생성
        String sessionUuid = java.util.UUID.randomUUID().toString();

        // AccessToken 생성
        Date accessExp = new Date(now + securityProperties.jwt().expireTime().access().toMillis());
        String accessToken = generateToken(subject, authorities, sessionUuid, issuedAt, accessExp);

        // RefreshToken 생성
        Date refreshExp = new Date(now + securityProperties.jwt().expireTime().refresh().toMillis());
        String refreshToken = generateToken(subject, null, sessionUuid, issuedAt, refreshExp);

        return JwtToken.of(SecurityConstants.Token.GRANT_TYPE.trim(), accessToken, refreshToken);
    }

    /**
     * 실제 JWT를 생성하는 내부 메서드
     *
     * @param subject     토큰 주체
     * @param authorities 권한 클레임 ({@code null} 일 경우 제외)
     * @param sessionUuid 세션 식별자
     * @param issuedAt    발급 시각
     * @param expiration  만료 시각
     * @return 생성된 JWT String
     */
    private String generateToken(String subject, String authorities, String sessionUuid, Date issuedAt, Date expiration) {
        var builder = Jwts.builder()
                .subject(subject)
                .claim(SecurityConstants.Token.SESSION_ID_CLAIM, sessionUuid)
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
        if (!StringUtils.hasText(token)) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_REQUIRED);
        }

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
     * <h3>만료된 토큰도 허용하는 Claims 파싱</h3>
     *
     * <h5>우선순위</h5>
     * <ul>
     *    <li>만료(Expired)는 허용하되, 서명/형식 오류는 즉시 차단</li>
     * </ul>
     *
     * @param token JWT
     * @return 서명이 유효한 경우 Claims (만료 토큰이면 {@link ExpiredJwtException}의 Claims 반환)
     */
    public Claims parseClaimsAllowedExpired(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_REQUIRED);
        }

        try {
            return getClaims(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
            log.warn("{} Invalid JWT signature or format: {}", SecurityConstants.Log.LOG_PREFIX, e.getMessage());
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("{} JWT token is unsupported: {}", SecurityConstants.Log.LOG_PREFIX, e.getMessage());
            throw new BaseException(BaseResponseStatus.UNSUPPORTED_JWT_TOKEN);
        }
    }

    /**
     * 토큰에서 인증 식별자(subject/session_uuid) 추출
     */
    public TokenIdentity extractTokenIdentity(String token) {
        return toIdentity(parseClaims(token));
    }

    /**
     * 만료된 토큰도 허용하여 인증 식별자 추출
     */
    public TokenIdentity extractTokenIdentityAllowedExpired(String token) {
        return toIdentity(parseClaimsAllowedExpired(token));
    }

    /**
     * claims에서 subject/session_uuid를 검증해 구조화함
     *
     * @param claims JWT Claims
     * @return TokenIdentity
     */
    private TokenIdentity toIdentity(Claims claims) {
        String subject = claims.getSubject();
        String sessionUuid = claims.get(SecurityConstants.Token.SESSION_ID_CLAIM, String.class);

        if (!StringUtils.hasText(subject) || !StringUtils.hasText(sessionUuid)) {
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN_CLAIM);
        }

        return new TokenIdentity(subject, sessionUuid);
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

    /**
     * Request Header의 'Authorization' 필드에서 토큰 정보 추출
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.Token.GRANT_TYPE)) {
            return bearerToken.substring(SecurityConstants.Token.GRANT_TYPE.length());
        }
        return null;
    }

    /**
     * 토큰에서 권한 클레임 문자열 추출
     */
    public String extractAuthorities(Claims claims) {
        Object auth = claims.get(SecurityConstants.Token.AUTHORITIES_CLAIM);
        if (auth == null) {
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN_CLAIM);
        }
        return auth.toString();
    }
}
