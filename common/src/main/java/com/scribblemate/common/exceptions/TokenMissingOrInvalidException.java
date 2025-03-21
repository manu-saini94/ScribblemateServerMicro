package com.scribblemate.common.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenMissingOrInvalidException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String message;

	public TokenMissingOrInvalidException() {
		super();
	}

	public TokenMissingOrInvalidException(String message) {
		this.message = message;
	}
	public TokenMissingOrInvalidException(Throwable cause) {
		super(cause);
	}
}
