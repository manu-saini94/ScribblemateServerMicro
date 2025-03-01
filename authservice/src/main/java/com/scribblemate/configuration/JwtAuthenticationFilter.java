package com.scribblemate.configuration;

import java.io.IOException;

import com.scribblemate.common.exceptions.TokenExpiredException;
import com.scribblemate.common.exceptions.TokenMissingOrInvalidException;
import com.scribblemate.services.JwtAuthenticationService;
import com.scribblemate.common.utility.UserUtils;
import com.scribblemate.common.utility.Utils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Order(2)
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtAuthenticationService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${auth.api.prefix}")
    private String authApiPrefix;
    private RequestMatcher skipMatcher;

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver,
                                   JwtAuthenticationService jwtService, UserDetailsService userDetailsService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        skipMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher(authApiPrefix + UserUtils.REGISTER_URI),
                new AntPathRequestMatcher(authApiPrefix + UserUtils.FORGOT_URI),
                new AntPathRequestMatcher(authApiPrefix + UserUtils.LOGIN_URI)
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return skipMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }
            Cookie[] cookiesArray = request.getCookies();
            String accessTokenString = null;
            String refreshTokenString = null;
            if (cookiesArray != null) {
                for (Cookie cookie : cookiesArray) {
                    if (Utils.TokenType.ACCESS_TOKEN.getValue().equals(cookie.getName())) {
                        accessTokenString = cookie.getValue();
                    } else if (Utils.TokenType.REFRESH_TOKEN.getValue().equals(cookie.getName())) {
                        refreshTokenString = cookie.getValue();
                    }
                }
                if (accessTokenString == null) {
                    throw new TokenMissingOrInvalidException("Access token not found in request cookies");
                } else if (!jwtService.isAccessToken(accessTokenString)) {
                    throw new TokenMissingOrInvalidException("Access token is Invalid!");
                } else if (jwtService.isTokenExpired(accessTokenString)) {
                    throw new TokenExpiredException("Access token has expired!");
                }
                if (refreshTokenString == null) {
                    throw new TokenMissingOrInvalidException("Refresh token not found in request cookies");
                } else if (!jwtService.isRefreshToken(refreshTokenString)) {
                    throw new TokenMissingOrInvalidException("Refresh token is Invalid!");
                } else if (jwtService.isTokenExpired(refreshTokenString)) {
                    throw new TokenExpiredException("Refresh token has expired!");
                }
            } else {
                throw new TokenMissingOrInvalidException("Cookies are missing from the request");
            }
            String userEmail = jwtService.extractUsername(accessTokenString);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(accessTokenString, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

}
