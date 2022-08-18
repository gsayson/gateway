package dev.projectcoda.gateway.data;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Coda user repository.
 * @author Gerard Sayson
 */
public interface UserRepository extends MongoRepository<User, String> {
}
