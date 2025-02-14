package com.scribblemate.dto;

import java.time.LocalDateTime;

import com.scribblemate.utility.UserUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {

	private Long id;
	
	private String fullName;

	private String email;

	private UserUtils.Status status;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private String profilePicture;

}
