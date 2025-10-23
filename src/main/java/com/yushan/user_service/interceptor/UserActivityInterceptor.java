package com.yushan.user_service.interceptor;

import com.yushan.user_service.event.UserActivityEventProducer;
import com.yushan.user_service.event.dto.UserActivityEvent;
import com.yushan.user_service.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class UserActivityInterceptor implements HandlerInterceptor {

    @Autowired
    private UserActivityEventProducer userActivityEventProducer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("UserActivityInterceptor triggered for URL: {}", request.getRequestURL());
        try {
            UUID userId = getUserId();
            if (userId != null) {
                UserActivityEvent event = new UserActivityEvent(userId, "user-service", request.getRequestURI(), request.getMethod(), LocalDateTime.now());
                userActivityEventProducer.sendUserActivityEvent(event);
            }
        } catch (Exception e) {
            // catch all exceptions to avoid affecting main business
            log.warn("Failed to update user activity: {}", e.getMessage());
        }

        return true;
    }

    private UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetailsService.CustomUserDetails) {
            String id = ((CustomUserDetailsService.CustomUserDetails) principal).getUserId();
            if (id != null) {
                try {
                    return UUID.fromString(id);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid user ID format: {}", id);
                    return null;
                }
            }
        }
        return null;
    }
}
