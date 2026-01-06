package com.mini.buting.api.member.repository;

import com.mini.buting.api.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * ID로 회원 조회 (상세 정보 포함)
     * - 대학, 단과대, 얼굴형, 성격 키워드를 한 번에 fetch join으로 가져옴
     */
    @Query("SELECT m FROM Member m " +
           "LEFT JOIN FETCH m.universityDomain ud " +
           "LEFT JOIN FETCH ud.university u " +
           "LEFT JOIN FETCH m.college c " +
           "LEFT JOIN FETCH m.faceShape fs " +
           "LEFT JOIN FETCH m.personalities p " +
           "WHERE m.id = :memberId")
    Optional<Member> findByIdWithDetails(@Param("memberId") Long memberId);

    /**
     * 닉네임으로 회원 조회 (상세 정보 포함)
     */
    @Query("SELECT m FROM Member m " +
           "LEFT JOIN FETCH m.universityDomain ud " +
           "LEFT JOIN FETCH ud.university u " +
           "LEFT JOIN FETCH m.college c " +
           "LEFT JOIN FETCH m.faceShape fs " +
           "LEFT JOIN FETCH m.personalities p " +
           "WHERE m.nickname = :nickname")
    Optional<Member> findByNicknameWithDetails(@Param("nickname") String nickname);
}
