/*
 * Copyright (C) Gerard Sayson, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

/**
 * A metadata object containing details about Gateway. Useful for verifying JWT tokens.
 * @param version The version of Gateway.
 * @param algorithm The JWT algorithm in use.
 * @param publicKey The public key in use.
 * @author Gerard Sayson
 */
public record GatewayMetadata(String version, String algorithm, String publicKey) implements Response {
}
