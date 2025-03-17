package com.scribblemate.exceptions.labels;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelAlreadyExistException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String message;

	public LabelAlreadyExistException() {
		super();
	}

	public LabelAlreadyExistException(String message) {
		this.message = message;
	}
	public LabelAlreadyExistException(Throwable cause){
		super(cause);
	}
	public LabelAlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
