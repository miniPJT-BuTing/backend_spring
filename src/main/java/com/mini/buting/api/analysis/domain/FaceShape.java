package com.mini.buting.api.analysis.domain;

import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 외부에서 기본 생성자 호출 방지
@AllArgsConstructor
@Table(name = "face_shape", uniqueConstraints = {
        @UniqueConstraint(name = "uk_face_shape_name", columnNames = {"name"})
})
@Comment("AI 분석 얼굴형 카테고리 테이블")
public class FaceShape extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("얼굴형 식별자")
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    @Comment("얼굴형 명칭 (예: 강아지상, 고양이상)")
    private String name;

    @Column(name = "nickname", nullable = false, length = 30)
    @Comment("얼굴형 별칭 (예: 복실복실 강아지상)")
    private String nickname;

    @Column(name = "description", nullable = false, length = 200)
    @Comment("닮은 동물 특징 설명")
    private String description;

    @Column(name = "image", nullable = false, length = 200)
    @Comment("닮은 동물 사진 S3 URL")
    private String image;
}
