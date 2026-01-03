package com.mini.buting.api.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum Gender {
    M("남성"),
    W("여성");

    private static final Map<String, Gender> GENDER_MAP = Collections.unmodifiableMap(
            Stream.of(values()).collect(Collectors.toMap(Gender::name, Function.identity()))
    );

    private final String description;

    public static Optional<Gender> toGender(String gender) {
        return Optional.ofNullable(GENDER_MAP.get(gender.toUpperCase(Locale.ROOT)));
    }
}
