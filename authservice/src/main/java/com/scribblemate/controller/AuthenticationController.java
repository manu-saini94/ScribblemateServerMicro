package com.scribblemate.controller;

import com.scribblemate.annotation.LoadUserContext;
import com.scribblemate.aspect.UserContext;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.scribblemate.dto.LoginDto;
import com.scribblemate.dto.RegistrationDto;
import com.scribblemate.common.dto.UserDto;
import com.scribblemate.entities.User;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.services.AuthenticationService;
import com.scribblemate.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("${auth.api.prefix}")
@CrossOrigin(origins = "${allowed.origin}", allowedHeaders = "*", allowCredentials = "true")
public class AuthenticationController {

    private final UserService userService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<UserDto>> register(@RequestBody RegistrationDto registerDto) {
        User registeredUser = authenticationService.signUp(registerDto);
        UserDto userResponseDto = userService.getUserDtoFromUser(registeredUser);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.USER_REGISTRATION_SUCCESS, userResponseDto));
    }

    @PostMapping("/forgot")
    public ResponseEntity<Boolean> forgotPassword(@RequestParam String email) {
        boolean isSent = authenticationService.forgot(email);
        return ResponseEntity.ok(isSent);
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<UserDto>> authenticate(@RequestBody LoginDto loginUserDto,
                                                        HttpServletResponse response) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto, response);
        UserDto userResponseDto = userService.getUserDtoFromUser(authenticatedUser);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.USER_LOGIN_SUCCESS, userResponseDto));
    }

    @LoadUserContext
    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<UserDto>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        User authUser = authenticationService.refreshAuthToken(request, response);
        UserDto userResponseDto = userService.getUserDtoFromUser(authUser);
        return ResponseEntity.ok().body(new SuccessResponse<>(HttpStatus.OK.value(),
                ResponseSuccessUtils.TOKEN_REFRESH_SUCCESS, userResponseDto));
    }

    @LoadUserContext
    @GetMapping("/validate")
    public ResponseEntity<SuccessResponse<UserDto>> validateUser() {
        User user = UserContext.getCurrentUser();
        UserDto userResponseDto = userService.getUserDtoFromUser(user);
        return ResponseEntity.ok().body(new SuccessResponse<>(HttpStatus.OK.value(),
                ResponseSuccessUtils.USER_VALIDATION_SUCCESS, userResponseDto));
    }

    @LoadUserContext
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Boolean>> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        boolean isLoggedOut = authenticationService.logoutAuthUser(request, response);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.USER_LOGOUT_SUCCESS, isLoggedOut));
    }

    @GetMapping("/authenticate")
    public ResponseEntity<SuccessResponse<UserDto>> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        UserDto userDto = userService.getUserDtoFromUser(currentUser);
        return ResponseEntity.ok().body(
                new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.USER_AUTHENTICATION_SUCCESS, userDto));
    }

}
