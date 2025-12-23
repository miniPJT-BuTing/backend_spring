package com.mini.buting.global.config;


import com.mini.buting.global.property.RedisProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;


/**
 * Redis Start Up Ping Logger
 * - 어플리케이션 시작 직후 Redis에 PING을 보내 연결/권한을 점검하고 결과를 로그로 남긴다.
 */
@Configuration
@RequiredArgsConstructor
public class RedisStartUpLogger {
    private static final Logger log = LoggerFactory.getLogger(RedisStartUpLogger.class);

    private final RedisProperties redisProperties;

    /* Application 시작 직후 1회 Redis PING 및 로그 출력 */
    @Bean
    public ApplicationRunner redisPingLogger(StringRedisTemplate template) {
        return args -> {
            RedisConnectionFactory factory = template.getConnectionFactory();
            String where = resolveLocation(factory);

            try (RedisConnection connection = Objects.requireNonNull(factory).getConnection()) {
                String pong = connection.ping();
                if ("PONG".equalsIgnoreCase(pong)) {
                    log.info("[Redis] Connected: {} (PING=PONG)", where);
                } else {
                    log.warn("[Redis] Ping responded non-PONG: {} (resp={})", where, pong);
                }
            } catch (Exception e) {
                log.error("[Redis] Connection/PING failed: {} - {}", where, e.getMessage(), e);
                if (redisProperties.failFast()) {
                    // fail-fast: true 면 Redis 미연결 시 예외로 부팅 중단
                    throw new IllegalStateException("[Redis] Redis fail-fast enabled: startup aborted.", e);
                }
            }
        };
    }

    /* ConnectionFactory에서 호스트/포트 등 위치 정보 추출 */
    private String resolveLocation(RedisConnectionFactory factory) {
        if (factory instanceof LettuceConnectionFactory l) {
            return "%s:%s/db%s%s".formatted(l.getHostName(), l.getPort(), l.getDatabase(), (l.isUseSsl() ? " (ssl)" : ""));
        }
        return factory.getClass().getSimpleName();
    }
}
