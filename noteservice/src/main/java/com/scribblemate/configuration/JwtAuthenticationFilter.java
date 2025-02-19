package com.scribblemate.configuration;

import java.io.IOException;
import com.scribblemate.entities.User;
import com.scribblemate.common.exceptions.TokenExpiredException;
import com.scribblemate.common.exceptions.TokenMissingOrInvalidException;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.repositories.UserRepository;
import com.scribblemate.common.services.JwtAuthenticationService;
import com.scribblemate.common.utility.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;

    private final JwtAuthenticationService jwtService;

    @Autowired
    private UserRepository userRepository;

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver,
                                   JwtAuthenticationService jwtService, UserDetailsService userDetailsService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookiesArray = request.getCookies();
        String accessTokenString = null;
        try {
            if (cookiesArray != null) {
                for (Cookie cookie : cookiesArray) {
                    if (Utils.TokenType.ACCESS_TOKEN.getValue().equals(cookie.getName())) {
                        accessTokenString = cookie.getValue();
                        if (accessTokenString == null) {
                            throw new TokenMissingOrInvalidException("Access token not found in request cookies");
                        } else if (!jwtService.isAccessToken(accessTokenString)) {
                            throw new TokenMissingOrInvalidException("Access token is Invalid!");
                        } else if (jwtService.isTokenExpired(accessTokenString)) {
                            throw new TokenExpiredException("Access token has expired!");
                        }
                    }
                }
            } else {
                throw new TokenMissingOrInvalidException("Cookies are missing from the request");
            }
            String userEmail = jwtService.extractUsername(accessTokenString);
            User user = userRepository.findByEmail(userEmail).orElseThrow(() ->
                    new UserNotFoundException("User not found with email : " + userEmail));
            UserContext.setCurrentUser(user);
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

}
