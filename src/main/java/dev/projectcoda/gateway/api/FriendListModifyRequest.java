/*
 * Copyright (C) Gerard Sayson, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * A request to modify the user's friend list.
 * @param add If {@code true}, adds the UUID to the friend list. Else, {@code false} removes the UUID.
 * @param friend The UUID of the friend.
 */
public record FriendListModifyRequest(boolean add, @NotNull UUID friend) {
}
