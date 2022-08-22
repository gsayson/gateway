/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * The Coda user repository.
 * @author Gerard Sayson
 */
@Repository
public interface UserRepository extends MongoRepository<User, UUID> { }
