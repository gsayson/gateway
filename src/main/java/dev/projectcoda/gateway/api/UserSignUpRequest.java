package dev.projectcoda.gateway.api;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * A user sign-up request.
 * @param username The desired username.
 * @param email The user's email.
 * @param password The password to use (in plain-text).
 * @author Gerard Sayson
 */
public record UserSignUpRequest(@NotNull String username, @NotNull @Email String email, @NotNull String password) {

	/**
	 * Creates a new {@link UserSignUpRequest}.
	 * @param username The desired username.
	 * @param password The password to use (in plain-text).
	 */
	public UserSignUpRequest {}

}
