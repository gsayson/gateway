/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.projectcoda.gateway.GatewayApplication;
import dev.projectcoda.gateway.data.Rank;
import dev.projectcoda.gateway.data.User;
import dev.projectcoda.gateway.data.UserRepository;
import dev.projectcoda.gateway.i18n.ErrorResponses;
import dev.projectcoda.gateway.security.AuthorizationService;
import dev.projectcoda.gateway.security.CaptchaService;
import dev.projectcoda.gateway.security.Permissions;
import dev.projectcoda.gateway.util.GravatarUtils;
import dev.projectcoda.gateway.util.HttpUtils;
import dev.projectcoda.gateway.util.SecurityUtils;
import dev.projectcoda.gateway.util.UserMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.*;

/**
 * The REST API controller for the gateway. All requests should use anonymous access.
 * @author Gerard Sayson
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/gateway", consumes = "application/json", produces = "application/json")
public class GatewayRestController {

	private final UserRepository repository;
	private final AuthorizationService authorizationService;

	/**
	 * The component constructor for {@link GatewayRestController}.
	 * @param repository The {@link UserRepository} that contains the users.
	 * @param authorizationService The {@link AuthorizationService} to use.
	 */
	public GatewayRestController(@Autowired UserRepository repository, @Autowired AuthorizationService authorizationService) {
		this.repository = repository;
		this.authorizationService = authorizationService;
	}

	/**
	 * Registers a user into the Gateway.
	 * @return a JSON response containing whether the user was
	 * successfully registered, and a message that is either the user's UUID (if the user
	 * was successfully logged in) or an error message.
	 */
	@PostMapping("/signup")
	public ResponseEntity<Response> signup(@Valid @RequestBody UserSignUpRequest request, @Autowired CaptchaService captchaService, @RequestParam(name = "g-recaptcha-response") String recaptchaResponse) {
		String captchaVerifyMessage = captchaService.verifyRecaptcha(recaptchaResponse);
		if (!captchaVerifyMessage.isEmpty()) {
			return ResponseEntity.badRequest().body(new ErrorResponse(ErrorResponses.CAPTCHA_ERROR));
		}
		if(repository.exists(UserMatchers.usernameExample(request.username()))) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorResponses.USERNAME_IN_USE));
		if(repository.exists(UserMatchers.emailExample(request.email()))) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorResponses.EMAIL_IN_USE));
		UUID uuid = UUID.randomUUID();
		repository.save(
				User.builder()
						.username(request.username())
						.email(request.email())
						.bio(null)
						.rating(1200)
						.uuid(uuid)
						.rank(Rank.UNRANKED)
						.permission(Permissions.USER)
						.password(SecurityUtils.encodeBCrypt(request.password()))
						.avatar(GravatarUtils.gravatar(request.email()))
						.build()
		);
		return ResponseEntity.ok(new UserSignUpResponse(uuid));
	}

	/**
	 * Logs a user into the Gateway.
	 * @return a JSON response containing whether the user was
	 * successfully logged in, and a message that is either the user's UUID (if the user
	 * was successfully logged in) or an error message.
	 */
	@PostMapping("/login")
	public ResponseEntity<Response> login(@Valid @RequestBody UserLogInRequest request) {
		Optional<User> userOptional = repository.findOne(UserMatchers.usernameExample(request.username()));
		if(userOptional.isPresent()) {
			User user = userOptional.get();
			if(SecurityUtils.matchesBCrypt(request.password(), user.getPassword())) {
				String refreshToken = authorizationService.issueRefreshToken(user);
				return ResponseEntity.ok(new UserLogInResponse(
						user.getUuid(),
						refreshToken,
						authorizationService.issueRegularToken(refreshToken)
				));
			} else {
				return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.BAD_CREDENTIALS));
			}
		} else {
			return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.BAD_CREDENTIALS));
		}
	}

	@PostMapping("/refresh")
	public ResponseEntity<Response> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ResponseEntity.ok(new RefreshTokenResponse(request.refreshToken(), authorizationService.issueRegularToken(request.refreshToken())));
	}

	/**
	 * Retrieves a user from the {@link UserRepository}.
	 * @param id The UUID of the user.
	 * @return the user details, else a 404 response if the user does not exist.
	 */
	@GetMapping(value = "/user/{id}", consumes = "*/*")
	public ResponseEntity<Response> getUser(@PathVariable String id) {
		Optional<User> optionalUser = repository.findById(UUID.fromString(id));
		return optionalUser.map(GatewayRestController::mapUserSafe).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/**
	 * Returns a JSON object containing the following:
	 * <ul>
	 *     <li>Whether the provided authorization token is valid.</li>
	 *     <li>The permissions of the provided authorization token.</li>
	 * </ul>
	 * For refresh tokens, the second property listed above will still be valid to use, however the first property will be {@code false}.
	 * <p><b>USE THE "{@link #metadata() GET /gateway/metadata}" ENDPOINT INSTEAD!</b> This allows validation to be done on the clientside.</p>
	 * <p>In future releases, this method will be removed soon.</p>
	 * @return a JSON object denoting some properties of the given authorization token, listed above. The {@link ResponseEntity} shim
	 * will always have a status of {@code 200 OK}.
	 * @deprecated This will be removed in favor of the {@link #metadata()} endpoint.
	 */
	@Deprecated(forRemoval = true)
	@PostMapping("/valid")
	public ResponseEntity<Response> valid(@Valid @RequestBody ValidTokenRequest request) {
		try {
			DecodedJWT jwt = authorizationService.decodeToken(request.token());
			return ResponseEntity.ok(new ValidTokenResponse(
					true,
					jwt.getClaim("refreshToken").asBoolean() ? "refresh" : "auth",
					jwt.getClaim("permissions").asList(String.class)
			));
		} catch(RuntimeException e) {
			return ResponseEntity.ok(new ValidTokenResponse(false, null, null));
		}
	}

	private static final GatewayMetadata metadata = new GatewayMetadata(GatewayApplication.VERSION, AuthorizationService.getAlgorithm().getName(), Base64.getEncoder().encodeToString(AuthorizationService.getPublicKey().getEncoded()));

	/**
	 * Returns information on Gateway. This consists of:
	 * <ul>
	 *     <li>The version of Gateway itself ({@code <semver>/<release-type>}).</li>
	 *     <li>The algorithm used to sign and verify JWTs.</li>
	 *     <li>The public key, encoded in Base64.</li>
	 * </ul>
	 * <p>This is cached statically.</p>
	 * @return metadata on Gateway.
	 */
	@GetMapping(consumes = "*/*")
	public ResponseEntity<Response> metadata() {
		return ResponseEntity.ok(metadata);
	}

	/**
	 * Updates the user according to the given user details. This requires
	 * {@link Permissions#ADMIN} authorization.
	 * <p>This endpoint is to be executed only by the game server, due to the destructive nature of this operation.</p>
	 * @param id The UUID of the user.
	 * @param request The new details of the {@link User}. This does not include its password.
	 * @return a {@code 204 No Content} response if successful, else a {@code 404 Not Found} response if the user is not found.
	 */
	@PutMapping("/user/{id}/server")
	public ResponseEntity<Response> updateUserAsServer(@PathVariable String id, @Valid @RequestBody UserShim request, @RequestHeader HttpHeaders httpHeaders) {
		Optional<String> bearerOptional = HttpUtils.getBearerToken(httpHeaders);
		if(bearerOptional.isPresent()) {
			try {
				DecodedJWT jwt = authorizationService.decodeToken(bearerOptional.get());
				if(!jwt.getClaim("permissions").asList(String.class).contains(Permissions.ADMIN)) {
					// we don't have the appropriate permissions, so we fail.
					return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.UNAUTHORIZED));
				}
			} catch(RuntimeException ignored) {
				return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.UNAUTHORIZED));
			}
		} else {
			return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.UNAUTHORIZED));
		}
		Optional<User> optionalUser = repository.findById(UUID.fromString(id));
		if(optionalUser.isPresent()) {
			repository.save(
					optionalUser.get().toBuilder()
							.bio(request.bio)
							.rating(request.rating)
							.rank(request.rank)
							.permissions(request.permissions)
							.uuid(request.uuid)
							.badges(request.badges)
							.email(request.email)
							.avatar(request.avatar)
							.won(request.won)
							.totalPlayed(request.totalPlayed)
							.build()
			);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Updates the user according to the given user details. This requires
	 * {@link Permissions#USER} authorization, and users can only update certain details.
	 * <p>This endpoint can and should only be executed by the client itself.</p>
	 * @param id The UUID of the user.
	 * @param request The new details of the {@link User}. This does not include its password.
	 * @return a {@code 204 No Content} response if successful, else a {@code 404 Not Found} response if the user is not found.
	 */
	@PutMapping("/user/{id}")
	public ResponseEntity<Response> updateUserAsUser(@PathVariable String id, @Valid @RequestBody UserUpdateRequest request, @RequestHeader HttpHeaders httpHeaders) {
		Optional<String> bearerOptional = HttpUtils.getBearerToken(httpHeaders);
		if(bearerOptional.isPresent()) {
			try {
				DecodedJWT jwt = authorizationService.decodeToken(bearerOptional.get()); // we get an exception if it's a bad token.
				if(!jwt.getSubject().equals(id)) throw new RuntimeException();
			} catch(RuntimeException ignored) {
				return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.UNAUTHORIZED));
			}
		} else {
			return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.UNAUTHORIZED));
		}
		if(repository.exists(UserMatchers.emailExample(request.email()))) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorResponses.EMAIL_IN_USE));
		Optional<User> optionalUser = repository.findById(UUID.fromString(id));
		if(optionalUser.isPresent()) {
			repository.save(
					optionalUser.get().toBuilder()
							.bio(request.bio())
							.email(request.email())
							.build()
			);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Modifies a user's friend list. This requires
	 * {@link Permissions#USER} authorization, and users can add only one friend at a time.
	 * <p>This endpoint can and should only be executed by the client itself.</p>
	 * <p>Calling this method when the user is trying to add himself does nothing!</p>
	 * @param id The UUID of the user.
	 * @param request The {@link FriendListModifyRequest}.
	 * @return a {@code 204 No Content} response if successful, else a {@code 404 Not Found} response if the user or friend is not found.
	 */
	@PutMapping("/user/{id}/friends")
	public ResponseEntity<Response> friendModify(@PathVariable String id, @RequestHeader HttpHeaders httpHeaders, @Valid @RequestBody FriendListModifyRequest request) {
		Optional<String> bearerOptional = HttpUtils.getBearerToken(httpHeaders);
		if(bearerOptional.isPresent()) {
			try {
				DecodedJWT jwt = authorizationService.decodeToken(bearerOptional.get());
				if(!jwt.getSubject().equals(id)) throw new RuntimeException();
			} catch(RuntimeException ignored) {
				return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.UNAUTHORIZED));
			}
		} else {
			return HttpUtils.unauthorized(new ErrorResponse(ErrorResponses.UNAUTHORIZED));
		}
		var uuid = UUID.fromString(id);
		var userO = repository.findById(uuid);
		if(userO.isPresent() && repository.existsById(request.friend())) {
			var user = userO.get();
			if(!user.getUuid().equals(request.friend())) {
				if(request.add()) {
					user.getFriends().add(request.friend());
				} else {
					user.getFriends().remove(request.friend());
				}
				repository.save(user);
			}
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}

	}

	// utility methods and classes

	/**
	 * Creates a {@link ResponseEntity} of a {@link User}, without exposing its BCrypt password field.
	 * @param user The user to create a {@link Response} from.
	 * @return a {@link ResponseEntity} that exposes everything of the user but the password.
	 */
	@SuppressWarnings("unused") // suppress the anonymous class and not this method
	private static ResponseEntity<Response> mapUserSafe(@NotNull @Valid User user) {
		return ResponseEntity.ok(new UserShim(
				user.getUsername(),
				user.getUuid(),
				user.getBio(),
				user.getBadges(),
				user.getRating(),
				user.getRank(),
				user.getPermissions(),
				user.getEmail(),
				user.getAvatar(),
				user.getFriends(),
				user.getWon(),
				user.getTotalPlayed()
		));
	}

	/**
	 * A shim of {@link User} that hides the password.
	 * <p>This is for internal use only, in {@link #mapUserSafe(User)}</p>
	 *
	 * @param username    The {@code username} parameter.
	 * @param uuid        The {@code uuid} parameter.
	 * @param bio         The {@code bio} parameter.
	 * @param badges      The {@code badges} parameter.
	 * @param rating      The {@code rating} parameter.
	 * @param rank        The {@code rank} parameter.
	 * @param permissions The {@code permissions} parameter.
	 * @param email       The {@code email} parameter.
	 * @param avatar      The {@code avatar} parameter.
	 * @param friends     The {@code friends} parameter.
	 * @param won         The {@code won} parameter.
	 * @param totalPlayed The {@code totalPlayed} parameter.
	 */
	private record UserShim(
			String username,
			UUID uuid,
			String bio,
			Set<String> badges,
			int rating,
			Rank rank,
			List<String> permissions,
			String email,
			URL avatar,
			Set<UUID> friends,
			long won,
			long totalPlayed
	) implements Response {}

}
