package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.Vote;
import com.mini.buting.api.chat.domain.chatroom.VoteOption;
import com.mini.buting.api.chat.dto.request.VoteOptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
    List<VoteOption> findByVote(Vote vote);
}
