/**
 * 
 */
package com.poc.user.exception;

/**
 * Exception thrown when requested user does not exist
 * @author didel
 *
 */
public class UserNotFoundException extends RuntimeException {

	private static final String DEFAULT_MESSAGE = "User not found.";
	public UserNotFoundException() {
		super(DEFAULT_MESSAGE);
	}
	public UserNotFoundException(String message) {
		super(message);
	}
}
