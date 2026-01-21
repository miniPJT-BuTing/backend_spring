package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.VoteBallot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteBallotRepository extends JpaRepository<VoteBallot, Long> {
}
