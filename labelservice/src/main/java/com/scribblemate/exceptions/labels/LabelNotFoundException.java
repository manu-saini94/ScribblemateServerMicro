package com.scribblemate.exceptions.labels;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String message;

	public LabelNotFoundException() {
		super();
	}

	public LabelNotFoundException(String message) {
		this.message = message;
	}
	public LabelNotFoundException(Throwable cause){
		super(cause);
	}
	public LabelNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
