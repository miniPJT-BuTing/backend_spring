package com.mini.buting.api.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 회원 권한
 */
@Getter
@AllArgsConstructor
public enum MemberRole {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private static final Map<String, MemberRole> ROLE_MAP = Collections.unmodifiableMap(Stream.of(values())
            .collect(Collectors.toMap(MemberRole::name, Function.identity()))
    );
    private final String name;

    public static MemberRole toMemberRole(String roleName) {
        return ROLE_MAP.getOrDefault(roleName.toUpperCase(Locale.ROOT), GUEST);
    }
}
