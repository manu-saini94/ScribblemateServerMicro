package com.scribblemate.exceptions.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String message;

	public RegistrationException() {
		super();
	}

	public RegistrationException(String message) {
		this.message = message;
	}
}
