/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.security;

import java.util.Set;

/**
 * Holds constant {@link String}s that represent "roles". They are as follows:
 * <ul>
 *     <li><b>{@link #ADMIN dev.projectcoda.gateway.admin}</b> - Has {@link #MODERATOR} and {@link #USER} permissions, and can promote users to moderators, demote them back to users, and award badges at will.</li>
 *     <li><b>{@link #MODERATOR dev.projectcoda.gateway.moderator}</b> - Has {@link #USER} permissions, and can mute, kick or ban {@link #USER}s.</li>
 *     <li><b>{@link #USER dev.projectcoda.gateway.user}</b> - Can use authenticated services (such as matchmaking and rooms) on Project Coda.</li>
 * </ul>
 * @author Gerard Sayson
 */
public interface Permissions {

	/**
	 * Admin permissions. Admins can:
	 * <ul>
	 *     <li>get all {@link #MODERATOR} and {@link #USER} permissions</li>
	 *     <li>promote or demote moderators</li>
	 *     <li>award badges</li>
	 * </ul>
	 */
	String ADMIN = "dev.projectcoda.gateway.admin";

	/**
	 * Moderator permissions. Moderators can:
	 * <ul>
	 *     <li>mute users</li>
	 *     <li>kick users</li>
	 *     <li>ban users (permanently or temporarily)</li>
	 * </ul>
	 */
	String MODERATOR = "dev.projectcoda.gateway.moderator";

	/**
	 * User permissions. Users can access authenticated Project Coda game services
	 * such as matchmaking and rooms.
	 */
	String USER = "dev.projectcoda.gateway.user";

	/**
	 * Obtain all the permissions of a user from the given permission,
	 * directly and indirectly.
	 * @param permission A permission in {@link Permissions}.
	 * @return all the permissions that the given permission itself directly or indirectly
	 * has.
	 */
	@SuppressWarnings("unused")
	static Set<String> permissions(String permission) {
		return switch(permission) {
			case ADMIN -> Set.of(ADMIN, MODERATOR, USER);
			case MODERATOR -> Set.of(MODERATOR, USER);
			case USER -> Set.of(USER);
			default -> throw new IllegalArgumentException("given value is not listed in Permissions");
		};
	}

}
