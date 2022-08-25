/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

/**
 * A request to validate a token. The token should be an authorization token for it to be
 * valid. If it is a refresh token, the authorization token will be false.
 * @param token The token to validate.
 */
public record ValidTokenRequest(String token) {
}
