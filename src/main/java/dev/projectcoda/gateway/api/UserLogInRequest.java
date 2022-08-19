package dev.projectcoda.gateway.api;

import javax.validation.constraints.NotBlank;

/**
 * The request that is sent when a user wishes to log in.
 * @param username The username to use.
 * @param password The plaintext password to use.
 */
public record UserLogInRequest(@NotBlank String username, @NotBlank String password) implements Response {

	/**
	 * Creates a new {@link UserLogInRequest}.
	 * @param username The username to use.
	 * @param password The plaintext password to use.
	 */
	// constructor javadocs
	public UserLogInRequest {}

}
