package com.supernovapos.finalproject.booking.interceptor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.supernovapos.finalproject.auth.security.CustomUserDetailsService;
import com.supernovapos.finalproject.auth.security.JwtUtil;

@Component
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthChannelInterceptor.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthChannelInterceptor(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        // 只在 CONNECT 階段處理驗證
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 1) 先嘗試從 CONNECT native headers 讀取 Authorization / token
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || authHeader.isBlank()) {
                authHeader = accessor.getFirstNativeHeader("token");
            }

            // 2) fallback: 從 session attributes（HandshakeInterceptor 放入的 token）
            if ((authHeader == null || authHeader.isBlank())) {
                Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                if (sessionAttrs != null && sessionAttrs.get("token") != null) {
                    String tokenValue = sessionAttrs.get("token").toString();
                    if (!tokenValue.isBlank()) {
                        authHeader = "Bearer " + tokenValue;
                    }
                }
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                try {
                    String username = jwtUtil.extractUsername(jwt);
                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (jwtUtil.validateToken(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(auth);
                            // 若後續需要在同一執行緒使用 SecurityContext，則同步設定
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            logger.debug("WebSocket CONNECT authenticated for user {}", username);
                        } else {
                            logger.debug("Invalid JWT token in WebSocket CONNECT for token {}", jwt);
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("Exception while authenticating WebSocket CONNECT token", ex);
                }
            } else {
                logger.debug("No Bearer token found in CONNECT headers or session attributes");
            }
        }

        return message;
    }

    // 其餘方法保持預設行為（可選覆寫）
}