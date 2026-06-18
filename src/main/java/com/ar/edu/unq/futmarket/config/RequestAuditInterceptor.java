package com.ar.edu.unq.futmarket.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RequestAuditInterceptor implements HandlerInterceptor {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger("AUDIT");
    private static final String START_TIME_ATTRIBUTE = RequestAuditInterceptor.class.getName() + ".START_TIME";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startTimeAttribute = request.getAttribute(START_TIME_ATTRIBUTE);
        if (!(startTimeAttribute instanceof Long startTime)) {
            return;
        }

        long executionTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            uri = uri + "?" + queryString;
        }

        AUDIT_LOGGER.info(
                "method={} uri={} status={} executionTimeMs={} clientIp={}",
                request.getMethod(),
                uri,
                response.getStatus(),
                executionTimeMs,
                resolveClientIp(request)
        );
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}