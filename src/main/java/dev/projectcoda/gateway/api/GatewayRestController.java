package dev.projectcoda.gateway.api;

import dev.projectcoda.gateway.data.Rank;
import dev.projectcoda.gateway.data.User;
import dev.projectcoda.gateway.data.UserRepository;
import dev.projectcoda.gateway.util.UserMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * The REST API controller for the gateway. All requests should use anonymous access.
 * @author Gerard Sayson
 */
@RestController
@RequestMapping(value = "/gateway", consumes = "application/json", produces = "application/json")
public class GatewayRestController {

	private final UserRepository repository;

	/**
	 * The component constructor for {@link GatewayRestController}.
	 * @param repository The {@link UserRepository} that contains the users.
	 */
	public GatewayRestController(@Autowired UserRepository repository) {
		this.repository = repository;
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
	public ResponseEntity<Response> login() {
		return ResponseEntity.ok(new UserLoggedInResponse(UUID.randomUUID()));
	}

	/**
	 * Retrieves a user from the {@link UserRepository}.
	 * @param id The UUID of the user.
	 * @return the user details, else a 404 response if the user does not exist.
	 */
	@GetMapping(value = "/user/{id}", consumes = "*/*")
	public ResponseEntity<Response> user(@PathVariable String id) {
		Optional<User> optionalUser = repository.findById(UUID.fromString(id));
		return optionalUser.<ResponseEntity<Response>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

}
