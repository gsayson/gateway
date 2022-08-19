package dev.projectcoda.gateway.api;

import dev.projectcoda.gateway.data.Rank;
import dev.projectcoda.gateway.data.User;
import dev.projectcoda.gateway.data.UserRepository;
import dev.projectcoda.gateway.security.AuthorizationService;
import dev.projectcoda.gateway.security.Permissions;
import dev.projectcoda.gateway.util.SecurityUtils;
import dev.projectcoda.gateway.util.UserMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The REST API controller for the gateway. All requests should use anonymous access.
 * @author Gerard Sayson
 */
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
	public ResponseEntity<Response> signup(@RequestBody UserSignUpRequest request) {
		if(repository.exists(UserMatchers.usernameExample(request.username()))) return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("the username already exists"));
		UUID uuid = UUID.randomUUID();
		repository.save(
				User.builder()
						.username(request.username())
						.bio(null)
						.rating(1200)
						.uuid(uuid)
						.rank(Rank.UNRANKED)
						.permission(Permissions.USER)
						.password(SecurityUtils.encodeBCrypt(request.password())) // we have to do the work ourselves
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
	@GetMapping("/login")
	public ResponseEntity<Response> login(@RequestBody UserLogInRequest request) {
		Optional<User> userOptional = repository.findOne(UserMatchers.usernameExample(request.username()));
		if(userOptional.isPresent()) {
			User user = userOptional.get();
			if(SecurityUtils.matchesBCrypt(request.password(), user.getPassword())) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("bad credentials"));
			} else {
				String refreshToken = authorizationService.issueRefreshToken(user);
				return ResponseEntity.ok(new UserLogInResponse(
						user.getUuid(),
						refreshToken,
						authorizationService.issueRegularToken(refreshToken)
				));
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("bad credentials"));
		}
	}

	/**
	 * Retrieves a user from the {@link UserRepository}.
	 * @param id The UUID of the user.
	 * @return the user details, else a 404 response if the user does not exist.
	 */
	@GetMapping(value = "/user/{id}", consumes = "*/*")
	public ResponseEntity<Response> user(@PathVariable String id) {
		Optional<User> optionalUser = repository.findById(UUID.fromString(id));
		return optionalUser.map(GatewayRestController::mapUserSafe).orElseGet(() -> ResponseEntity.notFound().build());
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
		return ResponseEntity.ok(new UserQueryResponse(
				usernameT,
				uuidT,
				bioT,
				badgesT,
				ratingT,
				rankT,
				permissionsT
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
	private record UserQueryResponse(String username, UUID uuid, String bio, Set<String> badges, int rating, Rank rank, List<String> permissions) implements Response {}

}
