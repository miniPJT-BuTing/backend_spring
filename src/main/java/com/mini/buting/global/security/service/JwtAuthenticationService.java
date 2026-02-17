package com.mini.buting.global.security.service;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * <h2>JWT 기반 인증 객체 생성 서비스</h2>
 * <p>토큰 파싱/회원 조회/Authentication 조립 책임을 담당</p>
 */
@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    /**
     * <h3>JWT 토큰 기반 시큐리티 인증 객체 생성</h3>
     *
     * <p>토큰의 클레임에서 권한과 식별자를 추출한 뒤, DB 조회를 통해 최신 사용자 상태를 반영한 {@link AuthUser}를 생성.
     * 이 과정에서 사용자의 실제 PK를 인증 객체에 바인딩하여 이후 비즈니스 로직에서의 효율성을 확보.</p>
     * <hr/>
     * <h5>동작</h5>
     * <ol>
     *     <li>토큰 파싱 및 Claims 추출({@link JwtTokenProvider#parseClaims(String)})</li>
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
        Claims claims = jwtTokenProvider.parseClaims(token);
        String authorityClaim = jwtTokenProvider.extractAuthorities(claims);

        // 토큰 주체(sub) 추출
        String memberUuid = claims.getSubject();

        // DB 조회를 통해 principal 생성
        var member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        if (Boolean.TRUE.equals(member.getIsDeleted())) {
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        }

        AuthUser principal = AuthUser.from(member);
        var authorities = Arrays.stream(authorityClaim.split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
