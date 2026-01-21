package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
}
