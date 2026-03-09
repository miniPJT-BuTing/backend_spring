package com.mini.buting.api.chat.interceptor;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_LOWER = "authorization";
    private static final String SENDER_ID_SESSION_KEY = "senderId";
    private static final String GRANT_TYPE = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        StompCommand cmd = accessor.getCommand();
        if (cmd == null) return message;

        if (cmd == StompCommand.CONNECT) {
            authenticateAndSetUser(accessor);
        } else if (cmd == StompCommand.SEND || cmd == StompCommand.SUBSCRIBE) {
            Principal user = accessor.getUser();
            if (user == null) {
                authenticateAndSetUser(accessor);
            }
        }

        return message;
    }

    private void authenticateAndSetUser(StompHeaderAccessor accessor) {
        String raw = getAuthorizationHeader(accessor);

        String token = extractBearerToken(raw);
        Claims claims = jwtTokenProvider.parseClaims(token);

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN_CLAIM);
        }

        // 권한(선택): claims에 authorities 있을 때만
        Collection<SimpleGrantedAuthority> authorities = extractAuthoritiesSafe(claims);

        // Principal 세팅
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(subject, null, authorities);

        accessor.setUser(auth);

        // (선택) 세션 attribute에 senderId 저장 -> 이후 컨트롤러에서 쉽게 꺼낼 수 있음
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs != null) {
            try {
                sessionAttrs.put(SENDER_ID_SESSION_KEY, Long.parseLong(subject));
            } catch (NumberFormatException ignore) {
                throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
            }
        }
    }

    private String getAuthorizationHeader(StompHeaderAccessor accessor) {
        // Native STOMP header에서 Authorization 읽기
        String h = accessor.getFirstNativeHeader(AUTHORIZATION);
        if (h == null) h = accessor.getFirstNativeHeader(AUTHORIZATION_LOWER);

        if (h == null || h.isBlank()) {
            // CONNECT에 토큰 안 주면 여기서 막기
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_REQUIRED);
        }
        return h;
    }

    private String extractBearerToken(String rawHeader) {
        String grantType = GRANT_TYPE;
        if (rawHeader.startsWith(grantType)) {
            return rawHeader.substring(grantType.length()).trim();
        }
        // 혹시 "Bearer"만 있는 형태도 대비
        if (rawHeader.startsWith("Bearer ")) {
            return rawHeader.substring("Bearer ".length()).trim();
        }
        throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
    }

    private Collection<SimpleGrantedAuthority> extractAuthoritiesSafe(Claims claims) {
        try {
            String authStr = jwtTokenProvider.extractAuthorities(claims); // "ROLE_USER,ROLE_ADMIN" 같은 형태 가정
            return Arrays.stream(authStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // authorities 클레임이 없으면 빈 권한으로 처리(프로젝트 정책에 따라 막아도 됨)
            return List.of();
        }
    }
}
