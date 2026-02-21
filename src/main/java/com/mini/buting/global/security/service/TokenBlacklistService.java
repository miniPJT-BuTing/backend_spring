package com.mini.buting.global.security.service;

/**
 * <h2>JWT 토큰 무효화 관리 서비스</h2>
 * <p>로그아웃 또는 보안 이슈가 발생한 토큰을 블랙리스트에 등록하고 검증하는 역할 수행</p>
 */
public interface TokenBlacklistService {
    /**
     * 특정 토큰을 블랙리스에 등록하여 효력을 상실시킴
     */
    void register(String token);

    /**
     * 해당 토큰이 블랙리스트에 등록된 상태인지 확인
     *
     * @return 해당 토큰이 블랙리스트면 {@code true}를 반환, 그렇지 않으면 {@code false}를 반환
     */
    boolean isBlacklisted(String token);
}
