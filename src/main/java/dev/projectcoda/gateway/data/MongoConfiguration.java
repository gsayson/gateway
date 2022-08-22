/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.data;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.projectcoda.gateway.conf.GatewayConfiguration;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;

/**
 * Coda Gateway MongoDB configuration.
 * @author Gerard Sayson
 */
@Configuration
@SuppressWarnings("NullableProblems")
public class MongoConfiguration extends AbstractMongoClientConfiguration {

	private final GatewayConfiguration configuration;

	public MongoConfiguration(@Autowired GatewayConfiguration configuration) {
		this.configuration = configuration;
	}

	@NotNull
	@Override
	public String getDatabaseName() {
		return configuration.getMongoName();
	}

	@Override
	public MongoClient mongoClient() {
		ConnectionString connectionString = new ConnectionString(configuration.getMongoHost());
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Override
	public Collection<String> getMappingBasePackages() {
		return Collections.singleton("dev.projectcoda.gateway");
	}

}
