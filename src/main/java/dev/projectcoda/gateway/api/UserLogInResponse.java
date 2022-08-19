package dev.projectcoda.gateway.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * The response that is sent when a user is logged in.
 * @param uuid The user's UUID.
 * @param refreshToken The refresh JWT token to use when creating new JWT authorization tokens.
 * @param authToken A newly created JWT authorization token. This is for convenience on the client.
 * @author Gerard Sayson
 */
public record UserLogInResponse(@NotNull UUID uuid, @NotBlank String refreshToken, @NotBlank String authToken) implements Response {

	/**
	 * Creates a new {@link UserLogInResponse}.
	 * @param uuid The user's UUID.
	 * @param refreshToken The refresh JWT token to use when creating new JWT authorization tokens.
	 * @param authToken A newly created JWT authorization token. This is for convenience on the client.
	 */
	// constructor javadocs
	public UserLogInResponse {}

}
