/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.i18n;

/**
 * API error responses.
 * @author Gerard Sayson
 */
public abstract class ErrorResponses {

	private ErrorResponses() {
		//no instance
	}

	public static final String
			USERNAME_IN_USE = "Gateway.UsernameUsed",
			EMAIL_IN_USE = "Gateway.EmailUsed",
			CAPTCHA_ERROR = "Gateway.Captcha",
			BAD_CREDENTIALS = "Gateway.BadCredentials",
			UNAUTHORIZED = "Gateway.Unauthorized",
			PARAMETER_ERROR = "Gateway.Parameters";

}
