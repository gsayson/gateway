package dev.projectcoda.gateway.data;

import dev.projectcoda.gateway.api.Response;
import lombok.*;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a Coda user. Coda users have the following:
 * <ul>
 *     <li>A username</li>
 *     <li>A version 4, variant 2 {@link UUID UUID}</li>
 *     <li>(optional) A bio (in CommonMark markdown)</li>
 *     <li>Badges</li>
 *     <li>A matchmaking rating</li>
 *     <li>A matchmaking rank</li>
 * </ul>
 * <p>All fields in this class are not null unless specified otherwise.</p>
 * @author Gerard Sayson
 */
@Builder
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public final class User implements Response {

	/**
	 * The username of the user. This field cannot be blank.
	 */
	@NotNull
	@NotBlank
	private String username;

	/**
	 * The UUID of the user. This is a random (version 4, variant 2: Leach-Salz) UUID
	 * that can be obtained through {@link UUID#randomUUID()}.
	 */
	@Id
	@NotNull
	private UUID uuid;

	/**
	 * The bio of the user, in CommonMark markdown.
	 * This can be {@code null}.
	 */
	private String bio;

	/**
	 * An array containing IDs of badges. The badge IDs are as follows:
	 * <ul>
	 *     <li><b>{@code general.developer}</b> - The decorated user is a developer of Project Coda.</li>
	 *     <li><b>{@code general.friend}</b> - The decorated user is a friend of a developer of Project Coda.</li>
	 *     <li><b>{@code general.ko}</b> - The decorated user has KO-ed a developer of Project Coda.</li>
	 *     <li><b>{@code general.patron}</b> - The decorated user is supporting Project Coda financially.</li>
	 *     <li><b>{@code play.top3}</b> - The decorated player is one of the top three players in Coda.</li>
	 *     <li><b>{@code play.jack}</b> - The decorated player has won at least one match in <b>all</b> of the programming languages supported by Project Coda.</li>
	 * </ul>
	 */
	@NotNull
	@Singular
	private Set<String> badges;

	/**
	 * The matchmaking rating of the user. This is used to infer his rank, and by default
	 * it is 1200 ({@link Rank#C C}-rank).
	 * <p>This is calculated according to the Glicko-2 algorithm.</p>
	 */
	@PositiveOrZero
	private int rating;

	/**
	 * The matchmaking rank of the user. This is evaluated through {@link Rank}.
	 */
	@NotNull
	private Rank rank;

}
