/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.constraints.NotBlank;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utilities for secure random generation, and password hashing.
 * @author Gerard Sayson
 */
public final class SecurityUtils {

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	private SecurityUtils() {
		//no instance
	}

	/**
	 * Creates a random Base64-encoded {@link String} that is generated
	 * by {@link SecureRandom}
	 * @param length The length of the string to generate.
	 * @return a randomly generated {@link String}.
	 */
	public static String randomString(int length) {
		byte[] bytes = new byte[length];
		secureRandom.nextBytes(bytes);
		return Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * Encodes a password using the BCrypt scheme.
	 * @param password The password to encode.
	 * @return the encoded password.
	 */
	public static String encodeBCrypt(@NotBlank String password) {
		return encoder.encode(password);
	}

	/**
	 * Checks whether the given raw password and the BCrypt-encoded password match.
	 * @param password The raw password to compare.
	 * @param encoded The {@linkplain #encodeBCrypt(String) encoded} password.
	 * @return whether the two values match, according to {@link BCryptPasswordEncoder#matches(CharSequence, String)}.
	 */
	public static boolean matchesBCrypt(@NotBlank String password, @NotBlank String encoded) {
		return encoder.matches(password, encoded);
	}

}
