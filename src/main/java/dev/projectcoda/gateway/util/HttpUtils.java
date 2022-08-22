/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Utilities for operating on HTTP data structures.
 * @author Gerard Sayson
 */
public final class HttpUtils {

	private HttpUtils() {
		//no instance
	}

	/**
	 * Gets the raw bearer token (if present) and returns it, wrapped in an {@link Optional}.
	 * The {@link HttpHeaders#AUTHORIZATION Authorization} header for the Bearer scheme is as follows:
	 * <pre>{@code
	 * 	'Authorization' ':' WHITESPACE 'Bearer' TOKEN
	 * }</pre>
	 * Using this scheme, this method extracts the <code>TOKEN</code> part of the header
	 * <p>This is not decoded: For a decoding facility, see {@link dev.projectcoda.gateway.security.AuthorizationService#decodeToken(String)}.</p>
	 * @param httpHeaders The HTTP headers.
	 * @return an {@link Optional} containing the bearer token, if present.
	 */
	public static Optional<String> getBearerToken(@NotNull HttpHeaders httpHeaders) {
		String tok = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);
		return Optional.ofNullable(tok == null ? null : tok.split("Bearer ")[1]);
	}

	/**
	 * Creates a new {@link ResponseEntity} with the {@link HttpStatus#UNAUTHORIZED Unauthorized} status.
	 * @return a new {@link ResponseEntity}.
	 * @param <T> The body type.
	 */
	public static <T> ResponseEntity<T> unauthorized(T body) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

}
