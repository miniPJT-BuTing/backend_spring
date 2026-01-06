package com.mini.buting.api.university.domain;

import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "college", indexes = {
        @Index(name = "idx_college_name", columnList = "name")})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("단과대 정보 테이블")
public class College extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("단과대 식별자")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @Comment("단과대명 (예: 경영대학, 공과대학, 인문대학 등)")
    private String name;
}
