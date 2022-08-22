/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

import dev.projectcoda.gateway.data.User;

import javax.validation.constraints.NotBlank;

/**
 * A request to generate a regular token from a given JWT refresh token.
 * @param refreshToken The refresh token to generate a regular token from.
 * @see dev.projectcoda.gateway.security.AuthorizationService#issueRefreshToken(User)
 * @see dev.projectcoda.gateway.security.AuthorizationService#issueRegularToken(String)
 * @author Gerard Sayson
 */
public record RefreshTokenRequest(@NotBlank String refreshToken) {}
