package com.mini.buting.api.university.entity;

import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "university", uniqueConstraints = {
        @UniqueConstraint(name = "uk_university_name", columnNames = {"name"}),
        @UniqueConstraint(name = "uk_university_acronym", columnNames = {"acronym"})
}, indexes = {
        @Index(name = "idx_university_acronym", columnList = "acronym"),
        @Index(name = "idx_university_region", columnList = "region")
})
@Comment("대학교 정보 테이블")
public class University extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("대학교 고유 식별자")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @Comment("대학교 국문 명칭 (예: 부산대학교)")
    private String name;

    @Column(name = "acronym", length = 20)
    @Comment("대학교 영문 약칭 (예: PNU)")
    private String acronym;

    @Column(name = "region", length = 50)
    @Comment("소재지 (예: 부산, 대구)")
    private String region;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    @Comment("활성화 여부 (1: 활성, 0: 비활성)")
    private Boolean isActive = true;

}
