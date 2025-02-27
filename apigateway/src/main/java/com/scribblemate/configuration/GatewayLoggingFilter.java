package com.scribblemate.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Order(1)
@Component
@Slf4j
public class GatewayLoggingFilter extends OncePerRequestFilter {
    private String getHeadersAsString(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .filter(header -> !"Authorization".equalsIgnoreCase(header))
                .filter(header -> !"cookie".equalsIgnoreCase(header))
                .collect(Collectors.toMap(h -> h, request::getHeader))
                .toString();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Gateway received request: {} {} ", request.getMethod(), request.getRequestURI());
        log.info("from IP: {}", request.getRemoteAddr());
        log.info("with QueryParams: {}, Session: {}", request.getQueryString(),
                request.getSession(false) != null ? request.getSession(false).getId() : "No-Session");
        log.info("with Headers: {}", getHeadersAsString(request));
        log.info("with User-Agent: {}, Referer: {}", request.getHeader("User-Agent"),
                request.getHeader("Referer"));
        filterChain.doFilter(request, response);
    }
}