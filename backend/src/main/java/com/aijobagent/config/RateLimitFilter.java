package com.aijobagent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limit: 60 req/min per device/IP.
 * For production replace with Bucket4j + Redis.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_PER_MIN = 60;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    private static class Window {
        volatile long windowStart = Instant.now().getEpochSecond() / 60;
        AtomicInteger count = new AtomicInteger(0);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String key = request.getHeader("Authorization");
        if (key == null) key = request.getRemoteAddr();
        long curWindow = Instant.now().getEpochSecond() / 60;
        Window w = windows.computeIfAbsent(key, k -> new Window());
        synchronized (w) {
            if (w.windowStart != curWindow) {
                w.windowStart = curWindow;
                w.count.set(0);
            }
            if (w.count.incrementAndGet() > MAX_PER_MIN) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit 60 req/min\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
