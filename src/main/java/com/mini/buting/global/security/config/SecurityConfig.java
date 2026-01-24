package com.mini.buting.global.security.config;

import com.mini.buting.global.security.property.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityConfig {
}
