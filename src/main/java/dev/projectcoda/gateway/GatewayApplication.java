/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.validation.constraints.NotNull;
import java.security.Security;

@SpringBootApplication
public class GatewayApplication {

	@NotNull
	public static String VERSION = "1.0.0/production";

	public static void main(String[] args) {
		Security.setProperty("crypto.policy", "unlimited");
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(GatewayApplication.class, args);
	}

}
