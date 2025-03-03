package com.scribblemate.configuration;

import com.scribblemate.common.dto.UserDto;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.common.utility.UserUtils;
import com.scribblemate.services.ApiGatewayService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Order(2)
@Component
@Slf4j
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Autowired
    private ApiGatewayService apiGatewayService;
    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @Value("${auth.api.prefix}")
    private String authApiPrefix;

    @Value("${all.api.prefix}")
    private String allApiPrefix;
    private RequestMatcher skipMatcher;

    @PostConstruct
    public void init() {
        skipMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher(authApiPrefix + allApiPrefix));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return skipMatcher.matches(request);
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("Gateway Auth Filter ");
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }
            ResponseEntity<SuccessResponse<UserDto>> authResponse = apiGatewayService.authenticateUser();
            log.info("Authentication Response ", authResponse);
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            log.error("Exception while calling auth service from filter", exception);
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}
