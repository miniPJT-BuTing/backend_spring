package com.mini.buting.global.security.property;

import java.util.List;

public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials,
        List<String> exposedHeaders,
        Long maxAge
) {
}
