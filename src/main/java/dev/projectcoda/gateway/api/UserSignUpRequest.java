package dev.projectcoda.gateway.api;

/**
 * A user sign-up request.
 * @param username The desired username.
 * @param password The password to use (in plain-text).
 * @author Gerard Sayson
 */
public record UserSignUpRequest(String username, String password) {

	/**
	 * Creates a new {@link UserSignUpRequest}.
	 * @param username The desired username.
	 * @param password The password to use (in plain-text).
	 */
	public UserSignUpRequest {}

}
