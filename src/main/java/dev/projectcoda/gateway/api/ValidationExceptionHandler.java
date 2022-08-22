/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ValidationException;

/**
 * Contains error logic to handle {@link ValidationException}s.
 * <p>It is not useful to instantiate this class directly.</p>
 * @author Gerard Sayson
 */
@ControllerAdvice
public final class ValidationExceptionHandler {

	/**
	 * Returns a {@link ResponseEntity} of an {@link ErrorResponse} that contains the
	 * message of the thrown {@link ValidationException}.
	 * @param e The {@link ValidationException} that has been caught.
	 * @return a {@link ResponseEntity}.
	 */
	@ResponseBody
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<Response> handleValidationException(ValidationException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
	}

}
