package com.mini.buting.global.security.principal;

import com.mini.buting.api.member.domain.Member;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * <h2>인증된 사용자 정보 통합 인터페이스</h2>
 * <p>Spring Security의 {@link UserDetails}와 {@link OAuth2User}를 통합하여,
 * 일반 로그인과 소셜 로그인 여부에 상관없이 동일한 방식으로 사용자 정보를 참조합니다.</p>
 * <hr/>
 * <h5>사용 방법</h5>
 * <p>Controller에서 인증된 사용자 정보를 참조할 때 사용하세요. (<strong>컨트롤러에서 직접 구현체를 꺼내지 마세요</strong>)
 * {@code @AuthenticationPrincipal}을 통해 주입받으며, 추가적인 DB 조회 없이 사용자 식별자를 즉시 획득할 수 있습니다.</p>
 *
 * <pre>{@code
 * @GetMapping("/v1/member/me")
 * public BaseResponse<MemberProfileResponse> getMyProfile(@AuthenticationPrincipal AuthUser authUser) {
 *         MemberProfileResponse profile = memberService.getMemberProfile(authUser.getId());
 *         return BaseResponse.onSuccess(profile);
 * }
 * }</pre>
 * <hr/>
 * <h5>설계 의도</h5>
 * <ul>
 * <li>AccessToken에는 {@code uuid}만 노출하여 실제 DB PK를 은닉하기 위해</li>
 * <li>인증 필터 단계에서 한 번 조회된 {@code id}를 인증 객체에 담아둠으로써, 이후 비즈니스 로직에서의 중복 조회를 방지하기 위해</li>
 * <li>서비스 계층의 메서드가 외부 식별자(UUID)가 아닌, 내부 식별자(ID)를 파라미터로 받게 하여 도메인 로직을 단순화하기 위해</li>
 * </ul>
 */
public interface AuthUser extends UserDetails, OAuth2User {

    /**
     * Member 엔티티를 바탕으로 인증 객체를 생성하는 정적 팩토리 메서드
     * <p>상위 인터페이스에서 구현체를 직접 생성하여 반환함으로써, 호출부와 상세 구현체 간의 결합도를 낮추고자 했음.</p>
     *
     * @param member 사용자 엔티티
     * @return {@link AuthUser} 구현체
     */
    static AuthUser from(Member member) {
        return new PrincipalUser(
                member.getId(),
                member.getUuid(),
                member.getRole(),
                member.getIsDeleted(),
                java.util.Collections.emptyMap()
        );
    }

    /**
     * 외부 노출용 고유 식별자 반환
     * <li>인증 토큰에 담김</li>
     */
    String getUuid();

    /**
     * 데이터베이스 내부 식별자(PK)를 반환
     */
    Long getId();
}
