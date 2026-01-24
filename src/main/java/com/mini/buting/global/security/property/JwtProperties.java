package com.mini.buting.global.security.property;

import java.time.Duration;

public record JwtProperties(
        String secretKey,
        ExpireTime expireTime
) {
    public record ExpireTime(Duration access, Duration refresh) {
    }
}
