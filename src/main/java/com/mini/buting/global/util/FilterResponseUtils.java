package com.mini.buting.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.mini.buting.global.security.constant.SecurityConstants.Log;

/**
 * <h2>Security Filter 계층 전용 응답 생성 유틸리티</h2>
 * * <p>Filter Chain 내부에서 예외 발생 시, Servlet 응답({@link HttpServletResponse}) 객체에 직접 JSON 응답 데이터를 작성함</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class FilterResponseUtils {
    private final ObjectMapper objectMapper;

    public void sendErrorResponse(HttpServletResponse response, BaseResponseStatus status) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status.getHttpStatusCode().value());
        try {
            BaseResponse<Void> baseResponse = new BaseResponse<>(status);
            String responseBody = objectMapper.writeValueAsString(baseResponse);
            response.getWriter().write(responseBody);
        } catch (IOException e) {
            log.error("{}Failed to write error response to HttpServletResponse", Log.LOG_PREFIX, e);
        }
    }
}
