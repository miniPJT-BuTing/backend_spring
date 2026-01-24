package com.mini.buting.api.chat.domain.chatroom;

import com.mini.buting.api.chat.dto.request.NoticeUpsertRequest;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Access(AccessType.FIELD)
@NoArgsConstructor
public class Notice extends BaseTimeEntity {
    @Id
    @Column(name = "room_id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @Column(name = "place", nullable = false, length = 50)
    private String place;

    @Column(name = "meet_at", nullable = false)
    private LocalDateTime meetAt;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private Member updatedBy;

    public static Notice create(ChatRoom chatRoom, NoticeUpsertRequest request, Member member) {
        Notice notice = new Notice();
        notice.chatRoom = chatRoom;
        notice.place = request.place();
        notice.meetAt = request.meetAt();
        notice.description = request.description();
        notice.updatedBy = member;
        return notice;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void update(NoticeUpsertRequest request, Member updatedBy) {
        this.place = request.place();
        this.meetAt = request.meetAt();
        this.description = request.description();
        this.updatedBy = updatedBy;
    }
}
