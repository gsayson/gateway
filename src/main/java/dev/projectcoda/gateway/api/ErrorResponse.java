package dev.projectcoda.gateway.api;

import lombok.Getter;

import java.time.Instant;
import java.util.Date;

/**
 * An error response that can be serialized by Spring MVC
 * into JSON.
 * @author Gerard Sayson
 */
@Getter
public class ErrorResponse implements Response {

	/**
	 * The error message.
	 */
	private final String message;

	/**
	 * The timestamp of the error message.
	 */
	private final Date timestamp;

	public ErrorResponse(String message) {
		this.message = message;
		this.timestamp = Date.from(Instant.now());
	}

}
