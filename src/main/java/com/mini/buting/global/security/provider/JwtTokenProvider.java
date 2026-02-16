package com.mini.buting.global.security.provider;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.security.token.JwtToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
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
    private final MemberRepository memberRepository;

    public JwtTokenProvider(SecurityProperties securityProperties, MemberRepository memberRepository) {
        this.securityProperties = securityProperties;
        this.memberRepository = memberRepository;
        // Base64 인코딩된 SecretKey를 디코딩하여 HMAC-SHA 키 생성
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(securityProperties.jwt().secretKey()));
    }

    /**
     * 사용자의 식별값과 권한 정보를 바탕으로 Access & Refresh Token 세트 생성
     * <p>Refresh Token의 경우 권한 클레임을 생략</p>
     * <p>
     * 고유 sessionID를 생성하는 이유: 현재 redis RT키로 memberid만을 사용 중인데,
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
        Date accessExpirationDate = new Date(now + securityProperties.jwt().expireTime().access().toMillis());
        String accessToken = generateToken(subject, authorities, sessionUuid, issuedAt, accessExpirationDate);

        // RefreshToken 생성
        Date refreshExpirationDate = new Date(now + securityProperties.jwt().expireTime().refresh().toMillis());
        String refreshToken = generateToken(subject, null, sessionUuid, issuedAt, refreshExpirationDate);

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
     * 토큰의 유효성을 boolean 값으로 반환
     * <p>필터 계층에서 인증 여부 판단을 위해 호출됨</p>
     *
     * @param token 검증할 JWT
     * @return 유효할 경우 {@code true}, 그렇지 않다면 {@code false}
     * @implNote TODO: 필터 계층에서 내부 헬퍼 메소드로 옮겨도 될 듯 함.
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
     * <h3>JWT 토큰 기반 시큐리티 인증 객체 생성</h3>
     *
     * <p>토큰의 클레임에서 권한과 식별자를 추출한 뒤, DB 조회를 통해 최신 사용자 상태를 반영한 {@link AuthUser}를 생성.
     * 이 과정에서 사용자의 실제 PK를 인증 객체에 바인딩하여 이후 비즈니스 로직에서의 효율성을 확보.</p>
     * <hr/>
     * <h5>동작</h5>
     * <ol>
     *     <li>토큰 파싱 및 Claims 추출({@link #parseClaims(String)})</li>
     *     <li>권한 클레임({@code auth}) 검증 및 {@link GrantedAuthority} 변환</li>
     *     <li>subject({@code sub=memberUuid}) 기반으로 회원 조회</li>
     *     <li>탈퇴 회원({@code is_deleted=true})이면 인증 실패로 처리</li>
     *     <li>{@link AuthUser#from(Member)}로 principal 생성 후
     *          {@link UsernamePasswordAuthenticationToken} 반환</li>
     * </ol>
     *
     * @param token 검증된 JWT 토큰
     * @return SecurityContext에 저장될 인증 객체
     * @throws BaseException 권한 클레임 누락({@code INVALID_TOKEN_CLAIM}),
     *                       존재하지 않는 사용자({@code MEMBER_NOT_FOUND}),
     *                       탈퇴 회원 접근({@code INVALID_JWT_TOKEN})인 경우 발생
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (claims.get(SecurityConstants.Token.AUTHORITIES_CLAIM) == null) {
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN_CLAIM);
        }

        // 토큰의 권한 정보(auth) 추출
        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(SecurityConstants.Token.AUTHORITIES_CLAIM).toString().split(","))
                .map(SimpleGrantedAuthority::new).toList();

        // 토큰 주체(sub) 추출
        String memberUuid = claims.getSubject();

        // DB 조회를 통해 principal 생성
        var member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        if (Boolean.TRUE.equals(member.getIsDeleted())) {
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        }

        AuthUser principal = AuthUser.from(member);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
