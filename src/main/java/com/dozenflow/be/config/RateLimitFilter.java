package com.dozenflow.be.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight in-memory, per-IP fixed-window rate limiter for the public
 * write endpoints under /api/**. Not a substitute for a real gateway/WAF,
 * but blunts trivial abuse/scripted spam since the API has no auth.
 * Resets on redeploy — acceptable for this app's scale (single instance).
 * Threshold is configurable (see src/test/resources/application.properties)
 * so the full integration test suite — many MockMvc requests firing from
 * the same client "IP" within the same 60s window — doesn't trip it.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequestsPerWindow;
    private static final long WINDOW_MILLIS = 60_000;

    private final ConcurrentHashMap<String, Window> windowsByIp = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${rate-limit.max-requests-per-window:60}") int maxRequestsPerWindow) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        Window window = windowsByIp.computeIfAbsent(clientIp, ip -> new Window());

        if (window.tryConsume(maxRequestsPerWindow)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests, please try again later.\"}");
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        private final AtomicInteger count = new AtomicInteger(0);

        boolean tryConsume(int maxRequestsPerWindow) {
            long now = System.currentTimeMillis();
            long start = windowStart.get();
            if (now - start > WINDOW_MILLIS) {
                if (windowStart.compareAndSet(start, now)) {
                    count.set(0);
                }
            }
            return count.incrementAndGet() <= maxRequestsPerWindow;
        }
    }
}
