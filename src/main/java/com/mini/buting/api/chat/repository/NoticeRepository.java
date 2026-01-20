package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
