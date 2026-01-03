package com.mini.buting.api.university.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum DomainCategory {
    STUDENT("학생용"),
    STAFF("교직원용"),
    ALUMNI("졸업생용");

    private static final Map<String, DomainCategory> CATEGORY_MAP = Collections.unmodifiableMap(Stream.of(values())
            .collect(Collectors.toMap(DomainCategory::getDescription, Function.identity()))
    );
    private final String description;

    public static Optional<DomainCategory> toDomainCategory(String description) {
        return Optional.ofNullable(CATEGORY_MAP.get(description));
    }
}
