package com.scribblemate.controller;

import java.util.List;

import com.scribblemate.annotation.LoadUserContext;
import com.scribblemate.aspect.UserContext;
import com.scribblemate.common.utility.ResponseSuccessUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.scribblemate.dto.CollaboratorDto;
import com.scribblemate.dto.UserResponseDto;
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

	@GetMapping("/me")
	public ResponseEntity<User> authenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = (User) authentication.getPrincipal();
		return ResponseEntity.ok(currentUser);
	}

	@GetMapping("/get")
	public ResponseEntity<SuccessResponse> allUsers() {
		List<UserResponseDto> users = userService.getAllUsers();
		return ResponseEntity.ok()
				.body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.FETCH_ALL_USERS_SUCCESS, users));
	}

	@GetMapping("/exist/{email}")
	public ResponseEntity<SuccessResponse> checkCollaboratorExist(@PathVariable String email,
			HttpServletRequest httpRequest) {
		CollaboratorDto collaboratorDto = userService.checkForUserExist(email);
		return ResponseEntity.ok().body(new SuccessResponse(HttpStatus.OK.value(),
				ResponseSuccessUtils.CHECK_COLLABORATOR_EXIST, collaboratorDto));
	}

	@DeleteMapping("/delete")
	public ResponseEntity<SuccessResponse> deleteUser(HttpServletRequest httpRequest) {
		User user = UserContext.getCurrentUser();
		boolean isDeleted = userService.deleteUser(user);
		return ResponseEntity.ok()
				.body(new SuccessResponse(HttpStatus.OK.value(), ResponseSuccessUtils.USER_DELETE_SUCCESS, isDeleted));
	}
}
