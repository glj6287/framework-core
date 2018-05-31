package com.hywin.framework.exception;

public class SystemException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	// 错误code
	public String errorCode;

	public SystemException() {

	}

	public SystemException(String message) {
		super(message);
	}

	public SystemException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public SystemException(String errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	
}
