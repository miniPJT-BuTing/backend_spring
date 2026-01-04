package com.mini.buting.api.university.domain;

import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@Table(name = "major", indexes = {
        @Index(name = "idx_major_title", columnList = "title")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_major_title", columnNames = {"title"})
})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("학과 정보 테이블")
public class Major extends BaseTimeEntity {
    @Id
    @Comment("학과 고유 식별자")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    @Comment("학과 명칭 (예: 컴퓨터공학과)")
    private String title;


}
