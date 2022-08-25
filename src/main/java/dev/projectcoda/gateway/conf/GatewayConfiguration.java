/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * The external configuration class for Coda. Spring will scan for the following properties
 * declared here in {@code application.properties}:
 * <ul>
 *     <li><b>{@code coda.mongo-host}</b> - the <em>{@linkplain com.mongodb.ConnectionString Connection String}</em> of the backing MongoDB database.</li>
 *     <li><b>{@code coda.mongodb-name}</b> - the name of the backing MongoDB database.</li>
 * </ul>
 * @author Gerard Sayson
 */
@Data
@Configuration
@ConfigurationProperties("coda")
public class GatewayConfiguration {

	/**
	 * The <em>Connection String</em> of the backing MongoDB database.
	 */
	@NotBlank
	private String mongoHost;

	/**
	 * The name of the backing MongoDB database.
	 */
	@NotBlank
	private String mongoName;

	/**
	 * The lifetime of Gateway-issued tokens, in hours.
	 * This must be above zero.
	 */
	@Positive
	private int tokenExpiration;

	/**
	 * The lifetime of refresh token used to issue Gateway tokens, in days.
	 * This must be above zero.
	 */
	@Positive
	private int refreshExpiration = 1;

	/**
	 * The reCAPTCHA secret to use for protection against bots.
	 */
	private String recaptchaSecret;

}
