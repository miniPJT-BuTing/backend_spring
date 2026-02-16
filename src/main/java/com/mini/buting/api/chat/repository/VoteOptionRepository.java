package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.Vote;
import com.mini.buting.api.chat.domain.chatroom.VoteOption;
import com.mini.buting.api.chat.dto.request.VoteOptionRequest;
import com.mini.buting.api.chat.dto.response.VoteOptionCountProjection;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
    List<VoteOption> findByVote(Vote vote);
    @Query("""
                select vo.optionId
                from VoteOption vo
                where vo.vote.id = :voteId
            """)
    List<Long> findOptionIdsByVoteId(@Param("voteId") Long voteId);

    @Query("""
                select
                    vo.optionId as optionId,
                    vo.optionText as text,
                    count(vb.id.memberId) as count
                from VoteOption vo
                left join VoteBallot vb
                    on vb.id.optionId = vo.optionId
                where vo.vote.id = :voteId
                group by vo.optionId, vo.optionText, vo.optionOrder
                order by vo.optionOrder
            """)
    List<VoteOptionCountProjection> findOptionCounts(
            @Param("voteId") Long voteId
    );

    @Modifying
    @Query("delete from VoteOption vo where vo.vote.id = :voteId")
    void deleteByVoteId(@Param("voteId") Long voteId);
}
