/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

import javax.validation.constraints.NotBlank;

/**
 * A response to {@link RefreshTokenRequest} that contains the refresh token used to generate
 * a new regular token, and that regular token itself.
 * @param refreshToken The refresh token.
 * @param authToken The regular token.
 * @author Gerard Sayson
 */
public record RefreshTokenResponse(@NotBlank String refreshToken, @NotBlank String authToken) implements Response {
}
