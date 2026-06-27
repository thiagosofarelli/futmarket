package com.ar.edu.unq.futmarket.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestAuditInterceptorTest {

    private RequestAuditInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new RequestAuditInterceptor();
    }

    @Test
    void preHandle_setsStartTimeAttributeAndReturnsTrue() {
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(request).setAttribute(argThat(name -> name.contains("START_TIME")), any(Long.class));
    }

    @Test
    void afterCompletion_noStartTimeAttribute_returnsEarlyWithoutLogging() {
        when(request.getAttribute(any())).thenReturn(null);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(request, never()).getRequestURI();
    }

    @Test
    void afterCompletion_startTimeAttributeIsNotLong_returnsEarly() {
        when(request.getAttribute(any())).thenReturn("not-a-long");

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(request, never()).getRequestURI();
    }

    @Test
    void afterCompletion_withValidStartTime_logsRequest() {
        long startTime = System.nanoTime() - 5_000_000L;
        when(request.getAttribute(any())).thenReturn(startTime);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/players");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(request).getRequestURI();
        verify(request).getMethod();
    }

    @Test
    void afterCompletion_withQueryString_appendsQueryToUri() {
        long startTime = System.nanoTime();
        when(request.getAttribute(any())).thenReturn(startTime);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/players");
        when(request.getQueryString()).thenReturn("league=PL&position=FORWARD");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(request).getQueryString();
    }

    @Test
    void afterCompletion_withXForwardedFor_usesFirstIp() {
        long startTime = System.nanoTime();
        when(request.getAttribute(any())).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/orders/buy");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2, 10.0.0.3");
        when(response.getStatus()).thenReturn(201);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(request).getHeader("X-Forwarded-For");
        verify(request, never()).getHeader("X-Real-IP");
    }

    @Test
    void afterCompletion_withXRealIp_usesItWhenNoXForwardedFor() {
        long startTime = System.nanoTime();
        when(request.getAttribute(any())).thenReturn(startTime);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/admin/bootstrap/all-data");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.100");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(request).getHeader("X-Real-IP");
        verify(request, never()).getRemoteAddr();
    }

    @Test
    void afterCompletion_noIpHeaders_usesRemoteAddr() {
        long startTime = System.nanoTime();
        when(request.getAttribute(any())).thenReturn(startTime);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/users/1");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.5");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(request).getRemoteAddr();
    }

    @Test
    void afterCompletion_withException_doesNotThrow() {
        long startTime = System.nanoTime();
        when(request.getAttribute(any())).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/orders/buy");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(500);

        interceptor.afterCompletion(request, response, new Object(), new RuntimeException("error"));

        verify(response).getStatus();
    }
}
