package dev.projectcoda.gateway.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.projectcoda.gateway.data.Rank;
import dev.projectcoda.gateway.data.User;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Utilities for {@link Example}{@code <}{@link User}{@code >}-related operations.
 * @author Gerard Sayson
 */
@SuppressWarnings("NullableProblems")
public final class UserMatchers {

	// A random UUID generated at first use. This is for non-UUID checks.
	private static final UUID randomUUID = UUID.randomUUID();

	/**
	 * An {@link ExampleMatcher} matches only the username of a {@link User}.
	 */
	private static final ExampleMatcher USERNAME_MATCHER = ExampleMatcher.matchingAny()
			.withMatcher("username", ExampleMatcher.GenericPropertyMatchers.ignoreCase());

	private static final LoadingCache<String, Example<User>> userCacheMap = CacheBuilder.newBuilder()
			.maximumSize(1250)
			.build(new CacheLoader<>() {
				@Override
				@NotNull
				public Example<User> load(@NotNull String key) {
					return Example.of(
							User.builder()
									.username(key)
									.bio(null)
									.uuid(randomUUID)
									.rank(Rank.UNRANKED)
									.rating(0)
									.build(),
							USERNAME_MATCHER
					);
				}
			});

	/**
	 * Creates an {@link Example} with a username, that strictly matches
	 * (case-insensitively) usernames only.
	 * <p>All {@link User} objects that are newly created as a result of calling
	 * this method are cached for performance.</p>
	 * @param username The username to match.
	 * @return an {@link Example}.
	 */
	@NotNull
	public static Example<User> usernameExample(@NotNull String username) {
		return userCacheMap.getUnchecked(username);
	}

}
