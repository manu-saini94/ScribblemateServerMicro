package com.scribblemate.controller;

import java.util.List;
import com.scribblemate.annotation.LoadUserContext;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.scribblemate.common.dto.CollaboratorDto;
import com.scribblemate.common.dto.UserDto;
import com.scribblemate.entities.User;
import com.scribblemate.common.responses.SuccessResponse;
import com.scribblemate.services.UserService;

@RestController
@LoadUserContext
@RequestMapping("${api.prefix}/users")
@CrossOrigin(origins = "${allowed.origin}", allowedHeaders = "*", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<List<UserDto>>> allUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.FETCH_ALL_USERS_SUCCESS, users));
    }

    @GetMapping("/exist/{email}")
    public ResponseEntity<SuccessResponse<CollaboratorDto>> checkCollaboratorExist(@PathVariable String email) {
        CollaboratorDto collaboratorDto = userService.checkForUserExist(email);
        return ResponseEntity.ok().body(new SuccessResponse<>(HttpStatus.OK.value(),
                ResponseSuccessUtils.CHECK_COLLABORATOR_EXIST_SUCCESS, collaboratorDto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse<Boolean>> deleteUser(@AuthenticationPrincipal User user) {
        boolean isDeleted = userService.deleteUser(user);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.USER_DELETE_SUCCESS, isDeleted));
    }

    @PutMapping("/update")
    public ResponseEntity<SuccessResponse<UserDto>> updateUser(@RequestBody UserDto userDto,
                                                               @AuthenticationPrincipal User user) {
        UserDto userDetailsDto = userService.updateUserDetails(userDto, user);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.USER_UPDATE_SUCCESS, userDetailsDto));
    }

    @PutMapping("/activate")
    public ResponseEntity<SuccessResponse<UserDto>> activateUser(@AuthenticationPrincipal User user) {
        UserDto userDetailsDto = userService.activateUser(user);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.USER_UPDATE_SUCCESS, userDetailsDto));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<SuccessResponse<UserDto>> deactivateUser(@AuthenticationPrincipal User user) {
        UserDto userDetailsDto = userService.deactivateUser(user);
        return ResponseEntity.ok()
                .body(new SuccessResponse<>(HttpStatus.OK.value(), ResponseSuccessUtils.USER_UPDATE_SUCCESS, userDetailsDto));
    }

}
