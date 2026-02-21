package com.mini.buting.global.security.principal;

import com.mini.buting.api.member.domain.MemberRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <h2>인증된 사용자 정보 구현체</h2>
 * <p>사용자 정보의 불변성을 보장하기 위해 Java {@code record} 타입을 설정함.</p>
 *
 * @param id         데이터베이스의 PK로, 서버 내부 로직 및 연관관계 처리에 사용
 * @param uuid       외부 노출용 고유 식별자
 * @param role       사용자의 권한 정보(GUEST, USER, ADMIN)
 * @param isDeleted  회원의 탈퇴 여부를 나타내며, {@link #isDeleted()} 상태 결정의 핵심 요소
 * @param attributes
 * @see AuthUser
 */
public record PrincipalUser(
        Long id,
        String uuid,
        MemberRole role,
        boolean isDeleted,
        Map<String, Object> attributes
) implements AuthUser {
    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * <p>{@link #getUsername()}과 동일하게 {@code uuid}를 반환하여 일관성 유지</p>
     */
    @Override
    public String getName() {
        return uuid;
    }

    /**
     * 사용자 권한 목록을 반환
     * <p>{@link MemberRole}에 정의된 이름을 기반으로 {@link  SimpleGrantedAuthority}를 생성(한 회원 당 하나의 역할을 가지도록 설계됨)</p>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getName()));
    }

    /**
     * @implNote 현재 소셜 로그인 전용 설계이므로 비밀번호를 관리하지 않음.
     * 향후 자체 로그인 도입 시 이 부분에 해싱된 비밀번호 필드를 추가할 것
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * Security 인증 프로세스에서 사용자를 식별하는 Unique 이름
     * <p>우리 서비스는 {@code uuid}를 주 외부 식별자로 사용하므로 이를 반환</p>
     */
    @Override
    public String getUsername() {
        return uuid;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 계정의 활성화 여부
     * <p>회원 테이블의 {@code is_deleted} 상태를 반전시켜 탈퇴한 회원의 접근을 차단</p>
     *
     * @return 탈퇴하지 않은 정상 회원인 경우 {@code true}
     */
    @Override
    public boolean isEnabled() {
        return !isDeleted;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
