package dev.projectcoda.gateway.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The REST API controller for use by the client.
 * @author Gerard Sayson
 */
@RestController
@RequestMapping("/matchmaking")
public class GatewayRestController {

	public ResponseEntity<?> login() {
		return ResponseEntity.ok("s");
	}

}
