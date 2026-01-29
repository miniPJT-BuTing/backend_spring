package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Modifying
    @Query("""
            update Vote v
               set v.status = com.mini.buting.api.chat.domain.chatroom.VoteStatus.CLOSED
             where v.status = com.mini.buting.api.chat.domain.chatroom.VoteStatus.OPEN
               and v.deadline is not null
               and v.deadline <= :now
        """)
    int closeExpiredVotes(@Param("now") LocalDateTime now);

    @Query("""
            select v
            from Vote v
            where v.status = com.mini.buting.api.chat.domain.chatroom.VoteStatus.OPEN
              and v.deadline is not null
              and v.deadline <= :now
        """)
    List<Vote> findExpiredVotes(@Param("now") LocalDateTime now);
}
