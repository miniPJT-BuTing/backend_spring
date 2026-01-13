package com.mini.buting.api.member.repository;

import com.mini.buting.api.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * 닉네임으로 회원 조회 (친구 요청 시 사용)
     */
    @Query("SELECT m FROM Member m WHERE m.nickname = :nickname AND m.isDeleted = false")
    Optional<Member> findByNickname(@Param("nickname") String nickname);

    /**
     * 이메일로 회원 조회
     */
    @Query("SELECT m FROM Member m WHERE m.universityEmail = :email " +
                    "AND m.universityDomain.domain = :domain AND m.isDeleted = false")
    Optional<Member> findByUniversityEmail(@Param("email") String email,
                    @Param("domain") String domain);

    /**
     * UUID로 회원 조회
     */
    @Query("SELECT m FROM Member m WHERE m.uuid = :uuid AND m.isDeleted = false")
    Optional<Member> findByUuid(@Param("uuid") String uuid);

    /**
     * 닉네임으로 회원 검색 (부분 검색 - 친구 찾기용)
     */
    @Query("SELECT m FROM Member m WHERE m.nickname LIKE %:nickname% " +
                    "AND m.isDeleted = false ORDER BY m.nickname")
    List<Member> searchByNickname(@Param("nickname") String nickname);

    /**
     * 특정 대학의 회원들 중 닉네임 검색
     */
    @Query("SELECT m FROM Member m WHERE m.nickname LIKE %:nickname% " +
                    "AND m.universityDomain.university.id = :universityId " +
                    "AND m.isDeleted = false ORDER BY m.nickname")
    List<Member> searchByNicknameAndUniversity(@Param("nickname") String nickname,
                    @Param("universityId") Long universityId);

    /**
     * 활성 회원 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
                    "FROM Member m WHERE m.id = :memberId AND m.isDeleted = false")
    boolean existsActiveById(@Param("memberId") Long memberId);
}
