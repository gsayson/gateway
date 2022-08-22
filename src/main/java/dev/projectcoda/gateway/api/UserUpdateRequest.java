package dev.projectcoda.gateway.api;

import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * A request to update the user's details. These will cover all of them.
 * @author Gerard Sayson
 */
public record UserUpdateRequest(@Nullable String bio, @NotNull @Email String email) {
}
