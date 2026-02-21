package com.mini.buting.api.chat.controller;

import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.dto.request.*;
import com.mini.buting.api.chat.dto.response.*;
import com.mini.buting.api.chat.service.*;
import com.mini.buting.api.matchRequest.domain.MatchRequest;
import com.mini.buting.api.matchRequest.repository.MatchRequestRepository;
import com.mini.buting.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final MatchRequestRepository matchRequestRepository;
    private final ChatReadNotifier chatReadNotifier;
    private final ChatRoomListService chatRoomListService;
    private final NoticeService noticeService;
    private final VoteService voteService;

    // 메시지 전송
    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(
            @Payload ChatMessageRequest message,
            @Header("senderId") Long senderId
    ) {
        chatService.sendMessage(message, senderId);
    }

    @MessageMapping("chat.read.{roomId}")
    public void markRead(
            @DestinationVariable String roomId,
            ChatReadRequest req,
            @Header("senderId") Long senderId
    ){
        chatRoomService.markAsRead(Long.parseLong(roomId), senderId, req.lastReadSeq());
        chatReadNotifier.broadcastRead(Long.parseLong(roomId), senderId, req.lastReadSeq());
    }

    @Operation(
            summary = "채팅방 입장(정보 조회)",
            description = """
                채팅방에 입장하며 채팅방 요약 정보, 공지(있으면), 참여 멤버 목록, 초기 메시지 목록을 조회합니다.
                
                - `senderId` 헤더로 요청자를 식별합니다.
                - `noticeInfo`는 공지가 없으면 null 입니다.
                - `messages.messages`는 초기 진입 시 비어 있을 수 있습니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": {
                                    "roomInfo": {
                                      "roomId": "1463426641706356736",
                                      "title": "채팅남팀",
                                      "memberCount": 4,
                                      "createdAt": "2026-01-21T15:55:00.536767",
                                      "lastMessageSeq": 5
                                    },
                                    "noticeInfo": null,
                                    "memberInfo": [
                                      {
                                        "memberId": 1,
                                        "nickname": "홍길동",
                                        "profileImage": null,
                                        "gender": "M",
                                        "isLeader": true,
                                        "universityName": "부산대학교",
                                        "collegeName": "공과대학",
                                        "isDeleted": false
                                      },
                                      {
                                        "memberId": 2,
                                        "nickname": "김영희",
                                        "profileImage": null,
                                        "gender": "W",
                                        "isLeader": false,
                                        "universityName": "부산대학교",
                                        "collegeName": "경영대학",
                                        "isDeleted": false
                                      }
                                    ],
                                    "messages": {
                                      "roomId": "1463426641706356736",
                                      "messages": [],
                                      "nextCursor": null,
                                      "hasMore": false
                                    }
                                  }
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping("/{roomId}")
    public BaseResponse<ChatRoomInfoResponse> enterChatroom(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,
            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "1"
            )
            @RequestHeader("senderId") Long senderId
    ){
        ChatRoomInfoResponse roomInfo = chatRoomService.enterChatroom(roomId, senderId);

        return BaseResponse.onSuccess(roomInfo);
    }

    // 이전 메시지 조회
    @Operation(
            summary = "이전 메시지 조회(커서)",
            description = """
                채팅방 메시지를 커서 기반으로 조회합니다.
                
                - beforeSeq가 없으면(null): 최근 메시지(최신) 목록을 조회합니다. (입장/초기 로딩)
                - beforeSeq가 있으면: beforeSeq 기준으로 더 이전 메시지를 조회합니다.
                - 응답의 nextCursor를 다음 요청의 beforeSeq로 사용하세요.
                - hasMore=true면 다음 페이지(더 과거)가 존재합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "초기 조회(beforeSeq 없음)",
                                            value = """
                                        {
                                          "httpStatus": "OK",
                                          "isSuccess": true,
                                          "message": "요청에 성공하였습니다.",
                                          "code": 200,
                                          "result": {
                                            "roomId": "1463426641706356736",
                                            "messages": [],
                                            "nextCursor": null,
                                            "hasMore": false
                                          }
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "이전 조회(beforeSeq 있음)",
                                            value = """
                                        {
                                          "httpStatus": "OK",
                                          "isSuccess": true,
                                          "message": "요청에 성공하였습니다.",
                                          "code": 200,
                                          "result": {
                                            "roomId": "1463426641706356736",
                                            "messages": [
                                              {
                                                "messageId": "msg_01",
                                                "roomId": 1463426641706356736,
                                                "messageSeq": 120,
                                                "type": "TEXT",
                                                "senderId": 2,
                                                "content": "이전 메시지",
                                                "payload": null,
                                                "createdAt": "2026-01-21T16:10:00.123",
                                                "messageStatus": "NORMAL",
                                                "unreadCount": 3
                                              }
                                            ],
                                            "nextCursor": 120,
                                            "hasMore": true
                                          }
                                        }
                                        """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/{roomId}/messages")
    public BaseResponse<ChatMessagesResponse> getMessages(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "2"
            )
            @RequestHeader("senderId") Long senderId,

            @Parameter(
                    description = """
                        커서(messageSeq). 
                        - 없으면(null): 최신 메시지 조회(입장/초기 로딩)
                        - 있으면: 해당 값 기준으로 더 이전 메시지 조회
                        """,
                    required = false,
                    example = "121"
            )
            @RequestParam(required = false) Long beforeSeq
    ) {
        return BaseResponse.onSuccess(chatRoomService.getMessages(roomId, senderId, beforeSeq));
    }

    // 채팅방 생성 (테스트용. 실서비스에서는 api 없음)
    @PostMapping
    public String createChatroom(@RequestParam Long matchRequestId) throws IllegalAccessException {
        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId).orElseThrow(() -> new IllegalAccessException());
        ChatRoom room = chatRoomService.createRoom(matchRequest);
        chatRoomService.sendWelcomeMessage(room.getRoomId(), room.getLeader().getId());
        return room.getRoomId() + " " + room.getTitle();
    }



    @Operation(
            summary = "채팅방 목록 조회",
            description = """
                사용자가 참여 중인 채팅방 목록을 조회합니다.
                
                - 각 채팅방별: 제목/인원/생성일/마지막 메시지 요약/안 읽은 개수(unreadCount)를 반환합니다.
                - lastMessage는 메시지가 없으면 null일 수 있습니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": [
                                    {
                                      "roomId": "1463426641706356736",
                                      "title": "채팅남팀",
                                      "memberCount": 4,
                                      "createdAt": "2026-01-21T15:55:00.536767",
                                      "lastMessage": {
                                        "preview": "오늘 몇시에 만남?",
                                        "sentAt": "2026-01-21T16:10:00.123",
                                        "seq": 120
                                      },
                                      "unreadCount": 3
                                    },
                                    {
                                      "roomId": "1463426641706356737",
                                      "title": "채팅여팀",
                                      "memberCount": 4,
                                      "createdAt": "2026-01-22T12:00:00.000000",
                                      "lastMessage": null,
                                      "unreadCount": 0
                                    }
                                  ]
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping
    public BaseResponse<List<ChatRoomResponse>> getChatRooms(
            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "2"
            )
            @RequestHeader("senderId") Long senderId
    ) {
        return BaseResponse.onSuccess(chatRoomListService.getChatRooms(senderId));
    }

    @Operation(
            summary = "채팅방 타이틀 변경",
            description = """
                채팅방의 제목(title)을 변경합니다.
                
                - `senderId`는 채팅방 멤버여야 하며, 보통 팀장(리더)만 변경 가능하도록 제한합니다(서비스 정책에 따름).
                - 요청 본문에 변경할 `title`을 전달합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": {
                                    "roomId": "1463426641706356736",
                                    "title": "새로운 채팅방 제목"
                                  }
                                }
                                """
                            )
                    )
            )
    })
    @PutMapping("/{roomId}")
    public BaseResponse<ChatRoomUpdateResponse> updateChatRoomTitle(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "2"
            )
            @RequestHeader("senderId") Long senderId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "변경할 채팅방 제목",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = """
                                {
                                  "title": "새로운 채팅방 제목"
                                }
                                """
                            )
                    )
            )
            @RequestBody ChatRoomUpdateRequest chatRoomUpdateRequest
    ) {
        ChatRoomUpdateResponse updateRoom =
                chatRoomService.updateChatRoomTitle(roomId, senderId, chatRoomUpdateRequest);
        return BaseResponse.onSuccess(updateRoom);
    }

    @Operation(
            summary = "공지 등록/수정",
            description = """
                채팅방 공지를 등록하거나 수정합니다. (Upsert)
                
                - 공지가 없으면 CREATED, 있으면 UPDATED로 처리합니다.
                - `senderId`는 채팅방 멤버여야 하며, 정책에 따라 리더만 허용될 수 있습니다.
                - meetAt은 ISO-8601(LocalDateTime) 형식으로 전달합니다. 예: 2026-02-10T19:30:00
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "등록/수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "CREATED 예시",
                                            value = """
                                        {
                                          "httpStatus": "OK",
                                          "isSuccess": true,
                                          "message": "요청에 성공하였습니다.",
                                          "code": 200,
                                          "result": {
                                            "action": "CREATED",
                                            "roomId": "1463426641706356736",
                                            "place": "서면역 2번 출구",
                                            "meetAt": "2026-02-10T19:30:00",
                                            "description": "늦으면 벌금 5천원",
                                            "updatedAt": "2026-02-01T13:10:00.000",
                                            "updatedBy": "홍길동",
                                            "updatedById": 1
                                          }
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "UPDATED 예시",
                                            value = """
                                        {
                                          "httpStatus": "OK",
                                          "isSuccess": true,
                                          "message": "요청에 성공하였습니다.",
                                          "code": 200,
                                          "result": {
                                            "action": "UPDATED",
                                            "roomId": "1463426641706356736",
                                            "place": "서면역 1번 출구",
                                            "meetAt": "2026-02-10T20:00:00",
                                            "description": "시간 30분 미룸!",
                                            "updatedAt": "2026-02-01T14:05:10.123",
                                            "updatedBy": "김영희",
                                            "updatedById": 2
                                          }
                                        }
                                        """
                                    )
                            }
                    )
            )
    })
    @PutMapping("/{roomId}/notice")
    public BaseResponse<NoticeResponse> upsertNotice(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "2"
            )
            @RequestHeader("senderId") Long senderId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "공지 등록/수정 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = """
                                {
                                  "place": "서면역 2번 출구",
                                  "meetAt": "2026-02-10T19:30:00",
                                  "description": "늦으면 벌금 5천원"
                                }
                                """
                            )
                    )
            )
            @RequestBody NoticeUpsertRequest request
    ) {
        return BaseResponse.onSuccess(noticeService.upsertNotice(roomId, senderId, request));
    }

    @Operation(
            summary = "공지 조회",
            description = """
                채팅방 공지를 조회합니다.
                
                - 공지가 없으면 404 또는 null 반환 여부는 서비스 정책에 따릅니다.
                - `senderId`는 채팅방 멤버여야 합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": {
                                    "roomId": "1463426641706356736",
                                    "place": "서면역 2번 출구",
                                    "meetAt": "2026-02-10T19:30:00",
                                    "description": "늦으면 벌금 5천원",
                                    "updatedAt": "2026-02-01T13:10:00.000",
                                    "updatedBy": "홍길동",
                                    "updatedById": 1
                                  }
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping("/{roomId}/notice")
    public BaseResponse<NoticeViewResponse> getNotice(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "2"
            )
            @RequestHeader("senderId") Long senderId
    ) {
        return BaseResponse.onSuccess(noticeService.getNotice(roomId, senderId));
    }

    @Operation(
            summary = "공지 삭제",
            description = """
                채팅방 공지를 삭제합니다.
                
                - 공지가 존재할 경우 삭제 처리합니다.
                - `senderId`는 채팅방 멤버여야 하며, 정책에 따라 리더만 삭제 가능할 수 있습니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": null
                                }
                                """
                            )
                    )
            )
    })
    @DeleteMapping("/{roomId}/notice")
    public BaseResponse<Void> deleteNotice(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "2"
            )
            @RequestHeader("senderId") Long senderId
    ) {
        noticeService.delete(roomId, senderId);
        return BaseResponse.onSuccess();
    }

    // 메시지 좋아요

    @Operation(
            summary = "투표 등록",
            description = """
                채팅방에 투표를 생성합니다.
                
                - multiple=true 이면 복수 선택 가능
                - anonymous=true 이면 투표자 정보가 비공개 처리됩니다.
                - deadLine 이후에는 투표가 자동 종료됩니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "투표 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": {
                                    "voteId": "1465555555555555555",
                                    "title": "어디서 만날까?",
                                    "description": "장소 투표",
                                    "multiple": false,
                                    "anonymous": false,
                                    "status": "OPEN",
                                    "options": [
                                      {
                                        "optionId": "1",
                                        "text": "서면",
                                        "order": 1,
                                        "count": 2,
                                        "voters": [
                                          {
                                            "memberId": 1,
                                            "nickname": "홍길동"
                                          }
                                        ]
                                      },
                                      {
                                        "optionId": "2",
                                        "text": "광안리",
                                        "order": 2,
                                        "count": 1,
                                        "voters": [
                                          {
                                            "memberId": 2,
                                            "nickname": "김영희"
                                          }
                                        ]
                                      }
                                    ],
                                    "mySelections": [1],
                                    "nickname": "홍길동",
                                    "creator": 1
                                  }
                                }
                                """
                            )
                    )
            )
    })
    @PostMapping("/{roomId}/vote")
    public BaseResponse<VoteInfoResponse> createVote(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "1"
            )
            @RequestHeader("senderId") Long senderId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "투표 생성 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = """
                                {
                                  "title": "어디서 만날까?",
                                  "description": "장소 투표",
                                  "options": [
                                    { "text": "서면", "order": 1 },
                                    { "text": "광안리", "order": 2 }
                                  ],
                                  "isMultiple": false,
                                  "isAnonymous": false,
                                  "deadLine": "2026-02-10T19:30:00"
                                }
                                """
                            )
                    )
            )
            @RequestBody VoteCreateRequest vote
    ) {
        return BaseResponse.onSuccess(
                voteService.createVote(roomId, senderId, vote)
        );
    }

    @Operation(
            summary = "투표 조회",
            description = """
                특정 투표(voteId)의 상세 정보를 조회합니다.
                
                - 응답에는 옵션 목록, 각 옵션 득표 수(count), 투표자 목록(voters), 그리고 요청자의 선택(mySelections)이 포함됩니다.
                - 익명 투표(anonymous=true)인 경우 voters가 비어있거나 마스킹될 수 있습니다(서비스 정책에 따름).
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": {
                                    "voteId": "1465976824046555136",
                                    "title": "어디서 만날까?",
                                    "description": "장소 투표",
                                    "multiple": false,
                                    "anonymous": false,
                                    "status": "OPEN",
                                    "options": [
                                      {
                                        "optionId": "1",
                                        "text": "서면",
                                        "order": 1,
                                        "count": 2,
                                        "voters": [
                                          { "memberId": 1, "nickname": "홍길동" }
                                        ]
                                      },
                                      {
                                        "optionId": "2",
                                        "text": "광안리",
                                        "order": 2,
                                        "count": 1,
                                        "voters": [
                                          { "memberId": 2, "nickname": "김영희" }
                                        ]
                                      }
                                    ],
                                    "mySelections": [1],
                                    "nickname": "홍길동",
                                    "creator": 1
                                  }
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping("/{roomId}/vote/{voteId}")
    public BaseResponse<VoteInfoResponse> getVoteInfo(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    description = "투표 ID ex)1465976824046555136",
                    required = true
            )
            @PathVariable String voteId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "2"
            )
            @RequestHeader("senderId") Long senderId
    ) {
        return BaseResponse.onSuccess(voteService.getVoteInfo(roomId, voteId, senderId));
    }


    @Operation(
            summary = "투표 참여",
            description = """
                특정 투표에 참여합니다.
                
                - 요청 본문의 options에는 선택한 옵션 ID 목록을 전달합니다.
                - multiple=false 인 투표에서 options는 1개만 허용됩니다.
                - 이미 투표한 경우, 기존 선택이 갱신됩니다(서비스 정책에 따름).
                - 투표 마감(deadLine)이 지난 경우 참여할 수 없습니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "투표 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": {
                                    "voteId": "1465976824046555136",
                                    "title": "어디서 만날까?",
                                    "description": "장소 투표",
                                    "multiple": true,
                                    "anonymous": false,
                                    "status": "OPEN",
                                    "options": [
                                      {
                                        "optionId": "1",
                                        "text": "서면",
                                        "order": 1,
                                        "count": 2,
                                        "voters": [
                                          { "memberId": 1, "nickname": "홍길동" }
                                        ]
                                      },
                                      {
                                        "optionId": "2",
                                        "text": "광안리",
                                        "order": 2,
                                        "count": 1,
                                        "voters": [
                                          { "memberId": 2, "nickname": "김영희" }
                                        ]
                                      }
                                    ],
                                    "mySelections": [1, 2],
                                    "nickname": "김영희",
                                    "creator": 1
                                  }
                                }
                                """
                            )
                    )
            )
    })
    @PutMapping("/{roomId}/vote/{voteId}")
    public BaseResponse<VoteInfoResponse> vote(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    description = "투표 ID ex)1465976824046555136",
                    required = true
            )
            @PathVariable String voteId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "2"
            )
            @RequestHeader("senderId") Long senderId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "선택한 투표 옵션 ID 목록",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = """
                                {
                                  "options": [1, 2]
                                }
                                """
                            )
                    )
            )
            @RequestBody VoteBallotCreateRequest vote
    ) {
        return BaseResponse.onSuccess(
                voteService.vote(roomId, voteId, senderId, vote)
        );
    }

    @Operation(
            summary = "투표 삭제",
            description = """
                채팅방 내 특정 투표를 삭제합니다.
                
                - 투표 생성자(creator) 또는 권한이 있는 멤버만 삭제할 수 있습니다(서비스 정책에 따름).
                - 삭제 시 해당 투표와 관련된 선택 내역도 함께 제거됩니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                {
                                  "httpStatus": "OK",
                                  "isSuccess": true,
                                  "message": "요청에 성공하였습니다.",
                                  "code": 200,
                                  "result": null
                                }
                                """
                            )
                    )
            )
    })
    @DeleteMapping("/{roomId}/vote/{voteId}")
    public BaseResponse<Void> deleteVote(
            @Parameter(
                    description = "채팅방 ID ex)1463426641706356736",
                    required = true
            )
            @PathVariable String roomId,

            @Parameter(
                    description = "투표 ID ex)1465976824046555136",
                    required = true
            )
            @PathVariable String voteId,

            @Parameter(
                    name = "senderId",
                    description = "요청자 회원 ID",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "1"
            )
            @RequestHeader("senderId") Long senderId
    ) {
        voteService.deleteVote(roomId, voteId, senderId);
        return BaseResponse.onSuccess();
    }

}
