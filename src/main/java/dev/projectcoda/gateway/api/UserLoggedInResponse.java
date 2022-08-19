package dev.projectcoda.gateway.api;

import java.util.UUID;

/**
 * The response that is sent when a user is logged in.
 * @param uuid The user's UUID.
 * @author Gerard Sayson
 */
public record UserLoggedInResponse(UUID uuid) implements Response {

	/**
	 * Creates a new {@link UserLoggedInResponse}.
	 * @param uuid The {@link UUID} of the user.
	 */
	// constructor javadocs
	public UserLoggedInResponse {}

}
