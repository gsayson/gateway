/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.api;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.projectcoda.gateway.data.Rank;
import dev.projectcoda.gateway.data.User;
import dev.projectcoda.gateway.data.UserRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
			return ResponseEntity.badRequest().body(new ErrorResponse(captchaVerifyMessage));
		}
		if(repository.exists(UserMatchers.usernameExample(request.username()))) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("the username already exists"));
		if(repository.exists(UserMatchers.emailExample(request.email()))) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("the email is already in use"));
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
				return HttpUtils.unauthorized(new ErrorResponse("bad credentials"));
			}
		} else {
			return HttpUtils.unauthorized(new ErrorResponse("bad credentials"));
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
	 * @return a JSON object denoting some properties of the given authorization token, listed above. The {@link ResponseEntity} shim
	 * will always have a status of {@code 200 OK}.
	 */
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
					return HttpUtils.unauthorized(new ErrorResponse("unauthorized"));
				}
			} catch(RuntimeException ignored) {
				return HttpUtils.unauthorized(new ErrorResponse("unauthorized"));
			}
		} else {
			return HttpUtils.unauthorized(new ErrorResponse("unauthorized"));
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
		System.out.println(bearerOptional);
		if(bearerOptional.isPresent()) {
			try {
				DecodedJWT jwt = authorizationService.decodeToken(bearerOptional.get()); // we get an exception if it's a bad token.
				if(jwt.getSubject().equals(id)) throw new RuntimeException();
			} catch(RuntimeException ignored) {
				return HttpUtils.unauthorized(new ErrorResponse("unauthorized"));
			}
		} else {
			return HttpUtils.unauthorized(new ErrorResponse("unauthorized"));
		}
		if(repository.exists(UserMatchers.emailExample(request.email()))) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("the email is already in use"));
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

	// utility methods and classes

	/**
	 * Creates a {@link ResponseEntity} of a {@link User}, without exposing its BCrypt password field.
	 * @param user The user to create a {@link Response} from.
	 * @return a {@link ResponseEntity} that exposes everything of the user but the password.
	 */
	@SuppressWarnings("unused") // suppress the anonymous class and not this method
	private static ResponseEntity<Response> mapUserSafe(@NotNull User user) {
		// do all the assignments here, so we don't encounter any funny problems
		String usernameT = user.getUsername();
		UUID uuidT = user.getUuid();
		String bioT = user.getBio();
		Set<String> badgesT = user.getBadges();
		int ratingT = user.getRating();
		Rank rankT = user.getRank();
		List<String> permissionsT = user.getPermissions();
		String emailT = user.getEmail();
		URL avatarT = user.getAvatar();
		return ResponseEntity.ok(new UserShim(
				usernameT,
				uuidT,
				bioT,
				badgesT,
				ratingT,
				rankT,
				permissionsT,
				emailT,
				avatarT
		));
	}

	/**
	 * A shim of {@link User} that hides the password.
	 * <p>This is for internal use only, in {@link #mapUserSafe(User)}</p>
	 * @param username The {@code username} parameter.
	 * @param uuid The {@code uuid} parameter.
	 * @param bio The {@code bio} parameter.
	 * @param badges The {@code badges} parameter.
	 * @param rating The {@code rating} parameter.
	 * @param rank The {@code rank} parameter.
	 * @param permissions The {@code permissions} parameter.
	 */
	private record UserShim(String username, UUID uuid, String bio, Set<String> badges, int rating, Rank rank, List<String> permissions, String email, URL avatar) implements Response {}

}
