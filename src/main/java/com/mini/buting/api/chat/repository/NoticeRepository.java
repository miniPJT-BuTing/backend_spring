package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.Notice;
import com.mini.buting.api.chat.dto.response.NoticeViewResponse;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("""
        select new com.mini.buting.api.chat.dto.response.NoticeViewResponse(
            nt.place,
            nt.meetAt,
            nt.description,
            nt.updatedAt,
            nt.updatedBy.nickname,
            nt.updatedBy.id
        )
        from Notice nt
        where nt.id = :roomId
    """)
    NoticeViewResponse findNoticeInfoById(@Param("roomId")Long roomId);
}
