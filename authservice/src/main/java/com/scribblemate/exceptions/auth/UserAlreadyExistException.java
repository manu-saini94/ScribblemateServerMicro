package com.scribblemate.exceptions.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAlreadyExistException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String message;

	public UserAlreadyExistException() {
		super();
	}

	public UserAlreadyExistException(String message) {
		this.message = message;
	}
}