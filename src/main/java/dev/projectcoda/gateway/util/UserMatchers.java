/*
 * Copyright (C) Gerard Sayson, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.projectcoda.gateway.data.Rank;
import dev.projectcoda.gateway.data.User;
import lombok.SneakyThrows;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
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
	private static final ExampleMatcher USERNAME_MATCHER = ofSingleProperty(User.class, "username", ExampleMatcher.GenericPropertyMatchers.ignoreCase());

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
									.email(SecurityUtils.randomString(10))
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

	/**
	 * An {@link ExampleMatcher} matches only the username of a {@link User}.
	 */
	private static final ExampleMatcher EMAIL_MATCHER = ofSingleProperty(User.class, "email", ExampleMatcher.GenericPropertyMatchers.ignoreCase());

	private static final LoadingCache<String, Example<User>> emailCacheMap = CacheBuilder.newBuilder()
			.maximumSize(1250)
			.build(new CacheLoader<>() {
				@Override
				@NotNull
				public Example<User> load(@NotNull String key) {
					return Example.of(
							User.builder()
									.username(SecurityUtils.randomString(10))
									.bio(null)
									.uuid(randomUUID)
									.rank(Rank.UNRANKED)
									.rating(0)
									.email(key)
									.build(),
							EMAIL_MATCHER
					);
				}
			});

	/**
	 * Creates an {@link Example} with an email, that strictly matches emails only (case-insensitive).
	 * <p>All {@link User} objects that are newly created as a result of calling
	 * this method are cached for performance.</p>
	 * @param email The email to match.
	 * @return an {@link Example}.
	 */
	@NotNull
	public static Example<User> emailExample(@NotNull @Email String email) {
		return emailCacheMap.getUnchecked(email);
	}

	/**
	 * Constructs an {@link ExampleMatcher} that matches only a single property. This will only
	 * work when the class {@code T} declares that field.
	 * @param clazz The class of {@code T}.
	 * @param propertyName The name of the field in {@code T} to match against.
	 * @param matcher The {@link org.springframework.data.domain.ExampleMatcher.GenericPropertyMatcher GenericPropertyMatcher} to use when evaluating whether properties' values match.
	 * @return an {@link ExampleMatcher} that matches only a single property.
	 * @throws NoSuchFieldException if the given property name is not a name of a field in the given class. This is hidden from the declaration
	 * due to the {@link SneakyThrows @SneakyThrows} annotation.
	 * @param <T> The type of the object.
	 */
	@SneakyThrows
	@SuppressWarnings({"JavadocDeclaration", "SameParameterValue"})
	private static <T> ExampleMatcher ofSingleProperty(Class<T> clazz, String propertyName, ExampleMatcher.GenericPropertyMatcher matcher) {
		Field[] fields = clazz.getDeclaredFields();
		List<String> fieldNames = Arrays.stream(fields).map(Field::getName).toList();
		if(!fieldNames.contains(propertyName)) {
			throw new NoSuchFieldException("no such field '" + propertyName + "' exists");
		}
		return ExampleMatcher.matchingAll()
				.withIgnorePaths(fieldNames.stream().filter(s -> !s.equals(propertyName)).toArray(String[]::new))
				.withMatcher(propertyName, matcher);
	}

}
