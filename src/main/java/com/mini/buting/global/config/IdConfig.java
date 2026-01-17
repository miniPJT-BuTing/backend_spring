package com.mini.buting.global.config;

import com.mini.buting.api.chat.service.SnowFlakeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdConfig {
    @Bean
    public SnowFlakeGenerator snowFlakeGenerator(@Value("${snowflake.node-id}") long nodeId) {
        return new SnowFlakeGenerator(nodeId);
    }
}
