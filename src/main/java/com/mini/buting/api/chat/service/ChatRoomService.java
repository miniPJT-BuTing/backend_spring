package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.domain.chatroom.ChatRoomMember;
import com.mini.buting.api.chat.repository.ChatRoomMemberRepository;
import com.mini.buting.api.chat.repository.ChatRoomRepository;
import com.mini.buting.api.matchRequest.domain.MatchRequest;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    // 채팅방 생성
    public ChatRoom createRoom(MatchRequest match) {

        match.validateAcceptedStatus();

        Team maleTeam;
        Team femaleTeam;

        Team requestTeam = match.getRequestTeam();
        if(requestTeam.getGender() == Team.Gender.MALE){
            maleTeam = requestTeam;
            femaleTeam = match.getTargetTeam();
        }
        else{
            maleTeam = match.getTargetTeam();
            femaleTeam = requestTeam;
        }

        if (chatRoomRepository.existsByMaleTeamAndFemaleTeam(maleTeam, femaleTeam)) {
            throw new IllegalStateException("이미 채팅방이 존재합니다.");
        }

        return createRoom(match.getRequestTeam().getTitle(), maleTeam, femaleTeam, match.getRequestTeam().getLeader());
    }

    private ChatRoom createRoom(String title, Team maleTeam, Team femaleTeam, Member leader) {
        ChatRoom room = ChatRoom.builder()
                .roomId(/* 생성 로직 */)
                .title(title)
                .maleTeam(maleTeam)
                .femaleTeam(femaleTeam)
                .leader(leader)
                .memberCount(maleTeam.getTeamSize().getSize() * 2)
                .lastMessage(null)
                .build();

        createMembers(room, maleTeam.getMembers());
        createMembers(room, femaleTeam.getMembers());

        return chatRoomRepository.save(room);
    }

    // 채팅방 목록 조회



    // 채팅방 생성 시 멤버랑 연결
    public void createMembers(ChatRoom chatRoom, List<Member> participants) {
        // 중복 방지(이미 만들어진 경우)도 하고 싶으면 exists 체크
        List<ChatRoomMember> rows = participants.stream()
                .map(m -> ChatRoomMember.builder()
                        .chatRoom(chatRoom)
                        .member(m)
                        .lastReadSeq(0L)
                        .build())
                .toList();

        chatRoomMemberRepository.saveAll(rows);
    }

    // 채팅방 목록 읽음 처리
    /*public void markAsRead(Long roomId, Long userId, Long newSeq) {
        if (newSeq == null) return;
        int updated = chatRoomMemberRepository.updateLastReadSeqMax(roomId, userId, newSeq);

        // 혹시 데이터가 없으면 예외
        if (updated == 0) {

        }
    }*/
}
