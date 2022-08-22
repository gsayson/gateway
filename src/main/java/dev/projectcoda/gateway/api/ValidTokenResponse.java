package dev.projectcoda.gateway.api;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A response to a {@link ValidTokenRequest}.
 * @param valid Whether the token is valid for use in authenticated Coda services.
 * @param permissions The permissions of the token. If the token's signature is wrong, it is to be {@code null}.
 * @see dev.projectcoda.gateway.security.Permissions
 * @author Gerard Sayson
 */
public record ValidTokenResponse(boolean valid, @Nullable List<String> permissions) implements Response {
}