/*
 * Copyright (C) Project Coda LLC, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.validation.constraints.NotNull;
import java.security.Security;

@Slf4j
@SpringBootApplication
public class GatewayApplication implements CommandLineRunner {

	@NotNull
	public static String VERSION = "1.1.1/production";

	public static void main(String[] args) {
		Security.setProperty("crypto.policy", "unlimited");
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(GatewayApplication.class, args);
	}

	/**
	 * Callback used to run the bean.
	 *
	 * @param args incoming main method arguments
	 */
	@Override
	public void run(String... args) {
		log.info("Gateway started - Project Coda rocks!");
	}

}
