package com.scribblemate.controller;

import java.util.List;

import com.scribblemate.annotation.LoadUserContext;
import com.scribblemate.aspect.UserContext;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import com.scribblemate.dto.RegistrationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.scribblemate.dto.CollaboratorDto;
import com.scribblemate.dto.UserDto;
import com.scribblemate.entities.User;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.services.UserService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@LoadUserContext
@RequestMapping("${api.prefix}/users")
@CrossOrigin(origins = "${allowed.origin}", allowedHeaders = "*", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/authenticate")
    public ResponseEntity<SuccessResponse> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        UserDto userDto = userService.getUserDtoFromUser(currentUser);
        return ResponseEntity.ok().body(
                new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.USER_AUTHENTICATION_SUCCESS, userDto));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse> allUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.FETCH_ALL_USERS_SUCCESS, users));
    }

    @GetMapping("/exist/{email}")
    public ResponseEntity<SuccessResponse> checkCollaboratorExist(@PathVariable String email) {
        CollaboratorDto collaboratorDto = userService.checkForUserExist(email);
        return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
                ResponseSuccessUtils.CHECK_COLLABORATOR_EXIST_SUCCESS, collaboratorDto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse> deleteUser() {
        User user = UserContext.getCurrentUser();
        boolean isDeleted = userService.deleteUser(user);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.USER_DELETE_SUCCESS, isDeleted));
    }

    @PutMapping("/update")
    public ResponseEntity<SuccessResponse> updateUser(@RequestBody UserDto userDto) {
        User user = UserContext.getCurrentUser();
        UserDto userDetailsDto = userService.updateUserDetails(userDto, user);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.USER_UPDATE_SUCCESS, userDetailsDto));
    }

    @PutMapping("/activate")
    public ResponseEntity<SuccessResponse> activateUser() {
        User user = UserContext.getCurrentUser();
        UserDto userDetailsDto = userService.activateUser(user);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.USER_UPDATE_SUCCESS, userDetailsDto));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<SuccessResponse> deactivateUser() {
        User user = UserContext.getCurrentUser();
        UserDto userDetailsDto = userService.deactivateUser(user);
        return ResponseEntity.ok()
                .body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.USER_UPDATE_SUCCESS, userDetailsDto));
    }

}
