package com.mini.buting.api.friend.repository;

import com.mini.buting.api.friend.domain.Friend;
import com.mini.buting.api.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * 특정 사용자의 친구 목록 조회 (페이징)
     */
    @Query("SELECT f FROM Friend f " +
           "WHERE f.member1.id = :memberId OR f.member2.id = :memberId " +
           "ORDER BY f.createdAt DESC")
    Page<Friend> findFriendsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 특정 사용자의 친구 목록 조회 (전체)
     */
    @Query("SELECT f FROM Friend f " +
           "WHERE f.member1.id = :memberId OR f.member2.id = :memberId")
    List<Friend> findAllFriendsByMemberId(@Param("memberId") Long memberId);

    /**
     * 두 사용자가 친구 관계인지 확인
     */
    @Query("SELECT COUNT(f) > 0 FROM Friend f " +
           "WHERE (f.member1.id = :memberId1 AND f.member2.id = :memberId2) " +
           "   OR (f.member1.id = :memberId2 AND f.member2.id = :memberId1)")
    boolean existsFriendshipBetween(@Param("memberId1") Long memberId1, 
                                   @Param("memberId2") Long memberId2);

    /**
     * 두 사용자 간의 친구 관계 조회
     */
    @Query("SELECT f FROM Friend f " +
           "WHERE (f.member1.id = :memberId1 AND f.member2.id = :memberId2) " +
           "   OR (f.member1.id = :memberId2 AND f.member2.id = :memberId1)")
    Optional<Friend> findFriendshipBetween(@Param("memberId1") Long memberId1, 
                                         @Param("memberId2") Long memberId2);

    /**
     * 특정 사용자의 친구 수 카운트
     */
    @Query("SELECT COUNT(f) FROM Friend f " +
           "WHERE f.member1.id = :memberId OR f.member2.id = :memberId")
    long countFriendsByMemberId(@Param("memberId") Long memberId);

    /**
     * 친구 검색 (닉네임으로)
     */
    @Query("SELECT f FROM Friend f " +
           "JOIN f.member1 m1 " +
           "JOIN f.member2 m2 " +
           "WHERE (f.member1.id = :memberId AND m2.nickname LIKE %:nickname%) " +
           "   OR (f.member2.id = :memberId AND m1.nickname LIKE %:nickname%) " +
           "ORDER BY f.createdAt DESC")
    Page<Friend> searchFriendsByNickname(@Param("memberId") Long memberId, 
                                        @Param("nickname") String nickname, 
                                        Pageable pageable);

    /**
     * 특정 멤버의 모든 친구들 조회
     */
    @Query("SELECT f FROM Friend f " +
                    "JOIN FETCH f.member1 m1 " +
                    "JOIN FETCH f.member2 m2 " +
                    "JOIN FETCH m1.universityDomain " +
                    "JOIN FETCH m2.universityDomain " +
                    "LEFT JOIN FETCH m1.college " +
                    "LEFT JOIN FETCH m2.college " +
                    "WHERE f.member1 = :member OR f.member2 = :member")
    List<Friend> findFriendsByMember(@Param("member") Member member);

    /**
     * 특정 멤버의 친구들을 검색 (닉네임 또는 이메일로)
     */
    @Query("SELECT f FROM Friend f " +
                    "JOIN FETCH f.member1 m1 " +
                    "JOIN FETCH f.member2 m2 " +
                    "JOIN FETCH m1.universityDomain ud1 " +
                    "JOIN FETCH m2.universityDomain ud2 " +
                    "LEFT JOIN FETCH m1.college " +
                    "LEFT JOIN FETCH m2.college " +
                    "WHERE (f.member1 = :member AND " +
                    "       (LOWER(m2.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "        LOWER(CONCAT(m2.universityEmail, '@', ud2.domain)) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
                    "OR (f.member2 = :member AND " +
                    "    (LOWER(m1.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "     LOWER(CONCAT(m1.universityEmail, '@', ud1.domain)) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    List<Friend> findFriendsByMemberAndKeyword(@Param("member") Member member,
                    @Param("keyword") String keyword);
}
