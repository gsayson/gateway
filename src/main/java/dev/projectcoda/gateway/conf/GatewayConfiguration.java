package dev.projectcoda.gateway.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

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

}
