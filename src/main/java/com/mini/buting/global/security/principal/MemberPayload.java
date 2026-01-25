package com.mini.buting.global.security.principal;

import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;

/**
 * <h2>JWT 클레임(Payload) 매핑 record</h2>
 *
 * <p>AccessToken 내부에 포함될 사용자의 최소 정보를 담는 DTO임
 * 보안 상 실제 DB PK({@code id}) 대신, {@code uuid}를 사용하여 외부 노출 시의 위험을 최소화하도록 설계됨</p>
 * <p>
 * <hr/>
 * <h3>사용 용도</h3>
 * <ul>
 * <li>토큰 생성: 로그인 성공 후 JWT 발급 시 Claim 정보를 구상할 때 사용</li>
 * <li>토큰 검증: Security Filter에서 JWT 파싱 후 사용자 식별 및 권한 확인을 위해 사용</li>
 * </ul>
 *
 * @param memberUuid  사용자 고유 식별자(UUID). 토큰 외부에 노출되어도 안전한 식별값(Auto Increment로 유추가 되지 않기 때문에)
 * @param authorities 쉼표(,)로 구분된 사용자의 권한 목록 ("ROLE_USER, ROLE_ADMIN")
 */
public record MemberPayload(
        String memberUuid,
        String authorities
) {
    @Builder
    public MemberPayload(String memberUuid, String authorities) {
        this.memberUuid = memberUuid;
        this.authorities = authorities;
    }

    public static MemberPayload from(AuthUser user) {
        return MemberPayload.builder()
                .memberUuid(user.getUuid())
                .authorities(
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(","))
                )
                .build();
    }
}
