package dev.projectcoda.gateway.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.projectcoda.gateway.conf.GatewayConfiguration;
import dev.projectcoda.gateway.data.User;
import lombok.Lombok;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * A service that handles JWT specifics and checks if a user is logged in and has certain permissions allowed.
 * @author Gerard Sayson
 */
@Slf4j
@Service
public class AuthorizationService {

	private static final ECPublicKey publicKey;
	private static final ECPrivateKey privateKey;
	private static final Algorithm algorithm;

	// Verifies both regular and refresh tokens.
	private static final JWTVerifier verifier;

	static {
		log.info("Initializing ECDSA keypair");
		try {
			KeyPair keyPair = KeyPairGenerator.getInstance("ECDSA").generateKeyPair();
			publicKey = (ECPublicKey) keyPair.getPublic();
			privateKey = (ECPrivateKey) keyPair.getPrivate();
			log.info("Base64 | Public key: {}", new String(Base64.getEncoder().encode(publicKey.getEncoded()), StandardCharsets.UTF_8));
			log.info("Base64 | Private key: {}", new String(Base64.getEncoder().encode(privateKey.getEncoded()), StandardCharsets.UTF_8));
		} catch(NoSuchAlgorithmException e) {
			throw Lombok.sneakyThrow(e);
		}
		log.info("Initializing JavaJWT Algorithm object");
		algorithm = Algorithm.ECDSA512(publicKey, privateKey);
		verifier = JWT.require(algorithm).withIssuer("Coda Gateway")
				.withAudience("projectcoda.dev")
				.withClaimPresence("refreshToken")
				.build();
	}
	private final GatewayConfiguration configuration;

	/**
	 * The {@link AuthorizationService} constructor.
	 * <p>Spring will instantiate this class, so there is no need to do it yourself.</p>
	 * @param configuration The {@link GatewayConfiguration} to use.
	 */
	public AuthorizationService(@Autowired GatewayConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Issues a <em>refresh</em> JWT token for a given {@link User}. The JWT token has the following properties:
	 * <ul>
	 *     <li>Its issuer claim is {@code Coda Gateway}</li>
	 *     <li>Its audience claim is {@code projectcoda.dev}</li>
	 *     <li>Its expiry is {@link GatewayConfiguration#getRefreshExpiration()} days from {@link Instant#now()}.</li>
	 *     <li>Its subject is the given user's {@link UUID}, in a string representation.</li>
	 *     <li>The private claim {@code permissions} contains the user's permissions.</li>
	 *     <li>The private claim {@code refreshToken} is {@code true}.</li>
	 * </ul>
	 * <p>This is only for use in {@link #issueRegularToken}, and this should be kept safeguarded.</p>
	 * @param user The {@link User} to issue a JWT token for.
	 * @return a JWT token that is subject to the above constraints.
	 * @see #issueRegularToken(String)
	 */
	public String issueRefreshToken(User user) {
		return JWT.create().withIssuer("Coda Gateway")
				.withAudience("projectcoda.dev")
				.withExpiresAt(Instant.now().plus(configuration.getRefreshExpiration(), ChronoUnit.DAYS))
				.withSubject(user.getUuid().toString())
				.withClaim("permissions", user.getPermissions())
				.withClaim("refreshToken", true)
				.sign(algorithm);
	}

	/**
	 * Issue a <em>regular</em> JWT token from the given {@linkplain #issueRefreshToken(User) refresh token}. It has the following properties:
	 * <ul>
	 *     <li>Its issuer claim is {@code Coda Gateway}</li>
	 *     <li>Its audience claim is {@code projectcoda.dev}</li>
	 *     <li>Its expiry is {@link GatewayConfiguration#getTokenExpiration()} hours from {@link Instant#now()}.</li>
	 *     <li>Its subject is the given user's {@link UUID}, in a string representation.</li>
	 *     <li>The private claim {@code permissions} contains the user's permissions.</li>
	 *     <li>The private claim {@code refreshToken} is {@code false}.</li>
	 * </ul>
	 * <p>The issued token can be used in all authenticated Coda services.</p>
	 * @param refreshToken The refresh token to issue.
	 * @return a JWT token that is subject to the above constraints.
	 * @throws com.auth0.jwt.exceptions.JWTVerificationException if an exception occurred while verifying the given refresh token.
	 * @see #issueRefreshToken(User)
	 * @see JWTVerifier#verify(String)
	 */
	public String issueRegularToken(String refreshToken) {
		DecodedJWT jwt = verifier.verify(refreshToken);
		return JWT.create().withIssuer("Coda Gateway")
				.withAudience("projectcoda.dev")
				.withExpiresAt(Instant.now().plus(configuration.getTokenExpiration(), ChronoUnit.HOURS))
				.withSubject(jwt.getSubject())
				.withClaim("permissions", List.of(jwt.getClaim("permissions").asArray(String.class)))
				.withClaim("refreshToken", false)
				.sign(algorithm);
	}

}
