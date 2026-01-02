package com.mini.buting.api.university.entity;

import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@Table(name = "university_domain", uniqueConstraints = {
        @UniqueConstraint(name = "uk_univ_domain_name", columnNames = {"domain"}) // 도메인 중복 방지
}, indexes = {
        @Index(name = "idx_univ_domain_university", columnList = "university_id")
})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("대학교 이메일 도메인 관리 테이블")
public class UniversityDomain extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("도메인 식별자")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false, foreignKey = @ForeignKey(name = "FK_univ_domain_university"))
    @Comment("소속 대학교 ID")
    private University university;

    @Column(name = "domain", nullable = false, length = 100)
    @Comment("이메일 도메인 (예: pnu.ac.kr)")
    private String domain;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    @Comment("도메인 카테고리 (STUDENT, STAFF 등)")
    private DomainCategory category = DomainCategory.STUDENT;

    @PrePersist
    public void prePersist() {
        if (this.category == null) {
            this.category = DomainCategory.STUDENT;
        }
    }
}
