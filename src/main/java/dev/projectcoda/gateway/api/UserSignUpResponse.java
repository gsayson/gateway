package dev.projectcoda.gateway.api;

import java.util.UUID;

/**
 * A user sign-up response.
 * @param uuid The UUID of the newly-created user.
 * @author Gerard Sayson
 */
public record UserSignUpResponse(UUID uuid) implements Response {}
