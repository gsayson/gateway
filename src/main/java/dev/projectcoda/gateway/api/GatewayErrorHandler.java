/*
 * Copyright (C) Gerard Sayson, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * An error handler for internal Gateway exceptions
 * @author Gerard Sayson
 */
@RestControllerAdvice
public class GatewayErrorHandler {

	/**
	 * Handles an exception.
	 * @param e The exception to handle.
	 * @return a {@link ResponseEntity} containing an {@link ErrorResponse} wrapping the exception. Its body will always
	 * start with {@code "ERROR:"} to signify an internal server error.
	 */
	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		return ResponseEntity.internalServerError().body(new ErrorResponse("ERROR: " + e.getMessage()));
	}

}
