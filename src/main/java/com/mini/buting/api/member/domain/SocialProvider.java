package com.mini.buting.api.member.domain;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum SocialProvider {
    KAKAO("kakao");

    private static final Map<String, SocialProvider> PROVIDER_NAME_MAP = Collections.unmodifiableMap(Stream.of(values())
            .collect(Collectors.toMap(SocialProvider::name, Function.identity())));

    private final String name;

    public static SocialProvider toProviderName(String providerName) {
        SocialProvider provider = (!StringUtils.hasText(providerName)) ? null : PROVIDER_NAME_MAP.get(providerName.toUpperCase(Locale.ROOT));
        return Optional.ofNullable(provider)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.SOCIAL_TYPE_NOT_SUPPORTED));
    }
}
