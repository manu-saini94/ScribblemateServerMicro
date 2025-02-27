package com.scribblemate.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import com.scribblemate.common.exceptions.TokenExpiredException;
import com.scribblemate.common.exceptions.TokenMissingOrInvalidException;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.common.services.EmailService;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import com.scribblemate.exceptions.*;
import com.scribblemate.common.utility.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import com.scribblemate.dto.LoginDto;
import com.scribblemate.dto.RegistrationDto;
import com.scribblemate.entities.User;
import com.scribblemate.repositories.UserRepository;
import com.scribblemate.common.utility.UserUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationService {

    private Random random = new Random(1000);
    @Value("${security.jwt.refresh-expiration-time}")
    private Long refreshTokenDurationMs;
    @Value("${security.jwt.access-expiration-time}")
    private Long accessTokenDurationMs;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtAuthenticationService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private AuthenticationManager authenticationManager;

    public User signUp(RegistrationDto input) {
        Optional<User> existingUser = userRepository.findByEmail(input.getEmail());
        if (existingUser.isPresent())
            throw new UserAlreadyExistException();
        User newUser = null;
        try {
            newUser = new User().setFullName(input.getFullName()).setEmail(input.getEmail())
                    .setPassword(passwordEncoder.encode(input.getPassword())).setStatus(Utils.Status.ACTIVE);
            User user = userRepository.save(newUser);
            log.info(ResponseSuccessUtils.USER_REGISTRATION_SUCCESS);
//            kafkaService.publishUserCreatedEvent(user);
            return user;
        } catch (Exception exp) {
            log.error(UserUtils.ERROR_PERSISTING_USER, newUser);
            throw new RegistrationException();
        }
    }

    public User authenticate(LoginDto input) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = new User();
        List<Object[]> fieldsList = userRepository.findSpecificFieldsByEmail(input.getEmail());
        if (fieldsList != null) {
            for (Object[] row : fieldsList) {
                Long id = (Long) row[0];
                LocalDateTime createdAt = (LocalDateTime) row[1];
                String email = (String) row[2];
                String fullName = (String) row[3];
                String profilePicture = (String) row[4];
                Utils.Status status = (Utils.Status) row[5];
                LocalDateTime updatedAt = (LocalDateTime) row[6];
                user.setId(id);
                user.setCreatedAt(createdAt);
                user.setEmail(email);
                user.setFullName(fullName);
                user.setProfilePicture(profilePicture);
                user.setStatus(status);
                user.setUpdatedAt(updatedAt);
            }
            return user;
        } else {
            log.error(UserUtils.ERROR_USER_NOT_FOUND);
            throw new UserNotFoundException();
        }
    }

    public User authenticate(@RequestBody LoginDto loginUserDto, HttpServletResponse response) {
        User authenticatedUser = authenticate(loginUserDto);
        if (authenticatedUser.getStatus().equals(Utils.Status.INACTIVE))
            throw new UserInactiveException();
        setTokensAndCookies(authenticatedUser, response);
        return authenticatedUser;
    }

    public void setTokensAndCookies(User user, HttpServletResponse response) {
        String jwtAccessToken = jwtService.generateToken(user.getEmail(), user.getId());
        Cookie newAccessTokenCookie = createAndReturnCookieWithAccessToken(jwtAccessToken);
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        Cookie newRefreshTokenCookie = createAndReturnCookieWithRefreshToken(jwtRefreshToken);
        addCookies(response, newAccessTokenCookie, newRefreshTokenCookie);
    }

    public Cookie createAndReturnCookieWithRefreshToken(String token) {
        Cookie newRefreshTokenCookie = new Cookie(Utils.TokenType.REFRESH_TOKEN.getValue(), token);
        newRefreshTokenCookie.setMaxAge((int) (refreshTokenDurationMs / 1000));
        return setCommonHeadersAndReturnCookie(newRefreshTokenCookie);
    }

    public Cookie createAndReturnCookieWithAccessToken(String token) {
        Cookie newAccessTokenCookie = new Cookie(Utils.TokenType.ACCESS_TOKEN.getValue(), token);
        newAccessTokenCookie.setMaxAge((int) (accessTokenDurationMs / 1000));
        return setCommonHeadersAndReturnCookie(newAccessTokenCookie);
    }

    public Cookie setCommonHeadersAndReturnCookie(Cookie cookie) {
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }

    public void addCookies(HttpServletResponse response, Cookie... cookies) {
        for (Cookie cookie : cookies) {
            StringBuilder cookieHeader = new StringBuilder();
            cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue()).append("; Max-Age=")
                    .append(cookie.getMaxAge()).append("; Path=").append(cookie.getPath());
            cookieHeader.append("; SameSite=none; Secure");
            response.addHeader("Set-Cookie", cookieHeader.toString());
        }
    }

    public boolean forgot(String email) {
        int otp = random.nextInt(10000);
        String subject = "OTP from notesy";
        String message = "<h1> OTP = " + otp + "</h1>";
        String to = email;
        boolean flag = emailService.sendEmail(subject, message, to);
        return flag;
    }

    public User refreshAuthToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshTokenValue = null;
        User user = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (Utils.TokenType.REFRESH_TOKEN.getValue().equals(cookie.getName())) {
                    refreshTokenValue = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshTokenValue == null) {
            throw new TokenMissingOrInvalidException("Refresh token is missing or invalid");
        }
        if (jwtService.isTokenExpired(refreshTokenValue)) {
            throw new TokenExpiredException("Refresh token has expired");
        } else {
            user = userService.getUserFromJwt(refreshTokenValue);
        }
        setTokensAndCookies(user, response);
        return user;
    }

    public boolean logoutAuthUser(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookiesArray = request.getCookies();
        if (cookiesArray != null) {
            for (Cookie cookie : cookiesArray) {
                if (Utils.TokenType.ACCESS_TOKEN.equals(cookie.getName()) || Utils.TokenType.REFRESH_TOKEN.equals(cookie.getName())) {
                    String tokenString = cookie.getValue();
                    if (tokenString == null) {
                        throw new TokenMissingOrInvalidException("Token is missing or invalid");
                    }
                    Cookie invalidCookie = new Cookie(cookie.getName(), null);
                    invalidCookie.setPath("/");
                    invalidCookie.setMaxAge(0);
                    response.addCookie(invalidCookie);
                }
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        SecurityContextHolder.clearContext();
        return true;
    }
}
