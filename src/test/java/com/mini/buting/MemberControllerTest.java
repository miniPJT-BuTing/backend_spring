package com.mini.buting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.buting.api.member.controller.MemberController;
import com.mini.buting.api.member.domain.Gender;
import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.service.MemberService;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.exception.BaseExceptionHandler;
import com.mini.buting.global.response.BaseResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Spring Boot 3.4+ 호환 테스트 코드
 * ✅ @MockBean → @Mock + @InjectMocks 변경
 * ✅ MockMvc 수동 설정
 * ✅ BaseExceptionHandler 추가
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("회원 프로필 조회 API 테스트")
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // MockMvc 수동 설정 (Spring Boot 3.4+ 호환)
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                        .setControllerAdvice(new BaseExceptionHandler()) // 예외 처리 핸들러 추가
                        .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("✅ 내 프로필 조회 성공")
    void getMyProfile_Success() throws Exception {
        // given
        Long userId = 1L;
        MemberProfileResponse mockResponse = createMockProfileResponse();

        when(memberService.getMemberProfile(userId)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/members/profile/me")
                                        .header("X-User-Id", userId)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.isSuccess").value(true))
                        .andExpect(jsonPath("$.code").value(200))
                        .andExpect(jsonPath("$.result.nickname").value("김원준"))
                        .andExpect(jsonPath("$.result.age").value(22))
                        .andExpect(jsonPath("$.result.universityName").value("부산대학교"));
    }

    @Test
    @DisplayName("✅ 특정 회원 프로필 조회 성공")
    void getMemberProfile_Success() throws Exception {
        // given
        Long memberId = 1L;
        MemberProfileResponse mockResponse = createMockProfileResponse();

        when(memberService.getMemberProfile(memberId)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/members/profile/{memberId}", memberId)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.isSuccess").value(true))
                        .andExpect(jsonPath("$.code").value(200))
                        .andExpect(jsonPath("$.result.memberId").value(1))
                        .andExpect(jsonPath("$.result.nickname").value("김원준"));
    }

    @Test
    @DisplayName("✅ 닉네임으로 프로필 조회 성공")
    void getMemberProfileByNickname_Success() throws Exception {
        // given
        String nickname = "김원준";
        MemberProfileResponse mockResponse = createMockProfileResponse();

        when(memberService.getMemberProfileByNickname(nickname)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/members/profile/search")
                                        .param("nickname", nickname)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.isSuccess").value(true))
                        .andExpect(jsonPath("$.code").value(200))
                        .andExpect(jsonPath("$.result.nickname").value("김원준"));
    }

    @Test
    @DisplayName("❌ 존재하지 않는 회원 조회 시 404 에러")
    void getMemberProfile_NotFound() throws Exception {
        // given
        Long nonExistentMemberId = 999L;

        when(memberService.getMemberProfile(nonExistentMemberId))
                        .thenThrow(new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/members/profile/{memberId}", nonExistentMemberId)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.isSuccess").value(false))
                        .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    @DisplayName("❌ 탈퇴한 회원 조회 시 403 에러")
    void getMemberProfile_DeletedUser() throws Exception {
        // given
        Long deletedMemberId = 5L;

        when(memberService.getMemberProfile(deletedMemberId))
                        .thenThrow(new BaseException(BaseResponseStatus.MEMBER_DELETED_USER));

        // when & then
        mockMvc.perform(get("/api/members/profile/{memberId}", deletedMemberId)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.isSuccess").value(false))
                        .andExpect(jsonPath("$.code").value(1003))
                        .andExpect(jsonPath("$.message").value("탈퇴한 사용자의 프로필은 조회할 수 없습니다."));
    }

    @Test
    @DisplayName("✅ 회원 존재 여부 확인 성공")
    void checkMemberExists_Success() throws Exception {
        // given
        Long memberId = 1L;

        when(memberService.existsActiveMember(memberId)).thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/members/{memberId}/exists", memberId)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.isSuccess").value(true))
                        .andExpect(jsonPath("$.result").value(true));
    }

    /**
     * 🛠️ 테스트용 Mock 프로필 응답 생성
     */
    private MemberProfileResponse createMockProfileResponse() {
        return MemberProfileResponse.builder()
                        .memberId(1L)
                        .nickname("김원준")
                        .age(22)
                        .gender(Gender.M)
                        .bio("안녕하세요! 부산대 컴공과 김원준입니다.")
                        .universityName("부산대학교")
                        .collegeName("공과대학")
                        .studentId("24학번")
                        .entryYear(2024)
                        .mbtiCode("ENFP")
                        .mbtiTitle("활동가")
                        .mbtiDescription("열정적이고 창의적이며 긍정적으로 삶을 바라보는 성격")
                        .personalities(List.of(
                                        MemberProfileResponse.PersonalityInfo.builder()
                                                        .code("EXTROVERTED")
                                                        .description("외향적")
                                                        .build(),
                                        MemberProfileResponse.PersonalityInfo.builder()
                                                        .code("CREATIVE")
                                                        .description("창의적인")
                                                        .build(),
                                        MemberProfileResponse.PersonalityInfo.builder()
                                                        .code("OPTIMISTIC")
                                                        .description("낙관적인")
                                                        .build()
                        ))
                        .faceShape("계란형")
                        .build();
    }
}
