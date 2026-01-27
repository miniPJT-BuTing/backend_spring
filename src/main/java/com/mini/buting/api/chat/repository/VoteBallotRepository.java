package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.VoteBallot;
import com.mini.buting.api.chat.dto.response.VoteOptionVoterProjection;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface VoteBallotRepository extends JpaRepository<VoteBallot, Long> {
    @Modifying
    @Query("""
            delete from VoteBallot vb
            where vb.id.voteId = :voteId
              and vb.id.memberId = :memberId
        """)
    void deleteByVoteIdAndMemberId(@Param("voteId") Long voteId,
                                   @Param("memberId") Long memberId);

    @Query("""
            select vb.id.optionId
            from VoteBallot vb
            where vb.id.voteId = :voteId
              and vb.id.memberId = :memberId
        """)
    List<Long> findOptionIdsByVoteAndMember(@Param("voteId") Long voteId,
                                            @Param("memberId") Long memberId);

    @Query("""
            select
                vb.id.optionId as optionId,
                m.id as memberId,
                m.nickname as nickname
            from VoteBallot vb
            join Member m on vb.id.memberId = m.id
            where vb.id.voteId = :voteId
        """)
    List<VoteOptionVoterProjection> findVotersByVoteId(
            @Param("voteId") Long voteId
    );
}
