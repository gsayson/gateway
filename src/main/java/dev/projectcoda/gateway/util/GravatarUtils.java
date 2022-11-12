/*
 * Copyright (C) Gerard Sayson, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.util;

import lombok.SneakyThrows;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Utilities for constructing Gravatar URLs.
 * @author Gerard Sayson
 */
public final class GravatarUtils {

	private GravatarUtils() {
		//no instance
	}

	/**
	 * The prefix for Gravatar's avatar service.
	 */
	public static final String AVATAR_PREFIX = "https://www.gravatar.com/avatar/";

	public static final String QUERY_STRING = "?d=identicon&r=pg";

	/**
	 * Returns a Gravatar link pointing to the avatar for a given user object, with the following constraints
	 * imposed through {@link #QUERY_STRING}.
	 * @return a Gravatar URL.
	 */
	@SneakyThrows
	public static URL gravatar(@NotNull @Email String email) {
		return new URL(
				AVATAR_PREFIX
				+ md5Hex(email)
				+ QUERY_STRING
		);
	}

	/**
	 * Gets the hexadecimal representation of a byte array.
	 * @param array The byte array.
	 * @return the hexadecimal representation of the given byte array.
	 */
	private static String hex(byte[] array) {
		StringBuilder sb = new StringBuilder();
		for(byte b : array) {
			sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
		}
		return sb.toString().intern();
	}

	/**
	 * Hashes an input string and returns it as a hexadecimal string.
	 * @param message The input to hash.
	 * @return a hexadecimal string representing the hashed input string.
	 */
	@SneakyThrows
	private static String md5Hex(String message) {
		MessageDigest md = MessageDigest.getInstance("MD5");
		return hex(md.digest(message.getBytes("CP1252")));
	}

}
