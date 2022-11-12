/*
 * Copyright (C) Gerard Sayson, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.data;

import lombok.*;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.net.URL;
import java.util.*;

/**
 * Represents a Coda user. Coda users have the following:
 * <ul>
 *     <li>A username</li>
 *     <li>A version 4, variant 2 {@link UUID UUID}</li>
 *     <li>(optional) A bio (in CommonMark markdown)</li>
 *     <li>Badges</li>
 *     <li>A matchmaking rating</li>
 *     <li>A matchmaking rank</li>
 *     <li>(not exposed) A BCrypt-encoded password</li>
 *     <li>A URL pointing to an image, which will be used as the avatar.</li>
 * </ul>
 * <p>All fields in this class are not null unless specified otherwise.</p>
 * @author Gerard Sayson
 */
@Getter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
@Builder(toBuilder = true)
public final class User {

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
	 *     <li><b>{@code staff.developer}</b> - The decorated user is a developer of Project Coda.</li>
	 *     <li><b>{@code staff.admin}</b> - The decorated user is an admin of Project Coda.</li>
	 *     <li><b>{@code staff.moderator}</b> - The decorated user is a moderator of Project Coda.</li>
	 *     <li><b>{@code special.friend}</b> - The decorated user is a friend of a developer of Project Coda.</li>
	 *     <li><b>{@code special.tester}</b> - The decorated user tested Project Coda before its first general release.</li>
	 *     <li><b>{@code general.devil}</b> - The decorated user has KO-ed a developer of Project Coda.</li>
	 *     <li><b>{@code general.patron}</b> - The decorated user is supporting Project Coda financially.</li>
	 *     <li><b>{@code play.top3}</b> - The decorated player is one of the top three players in Coda.</li>
	 *     <li><b>{@code play.taskcreator}</b> - The decorated user has created a Task that was accepted into the task database.</li>
	 *     <li><b>{@code play.codar}</b> - The decorated player has won at least one match in <b>all</b> of the programming languages supported by Project Coda.</li>
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

	/**
	 * A set of {@link dev.projectcoda.gateway.security.Permissions Permissions} that
	 * the user possesses.
	 */
	@Singular
	private List<String> permissions;

	@Email
	@NotNull
	private String email;

	/**
	 * The password of the user, encoded using {@link dev.projectcoda.gateway.util.SecurityUtils#encodeBCrypt(String) SecurityUtils.encodeBCrypt(String)}.
	 */
	@NotNull
	private String password;

	/**
	 * The avatar of the user. This can be obtained through {@link dev.projectcoda.gateway.util.GravatarUtils#gravatar(String)}.
	 */
	@NotNull
	@org.hibernate.validator.constraints.URL
	private URL avatar;

	/**
	 * The list of friends of a user. This is already initialized by default, so please do not modify
	 * this field.
	 */
	@NotNull
	@Builder.Default
	private Set<UUID> friends = new HashSet<>();

	/**
	 * The number of <em>online</em> games a player has won.
	 */
	@Builder.Default
	private long won = 0L;

	/**
	 * The number of <em>online</em> games a player has played.
	 */
	@Builder.Default
	private long totalPlayed = 0L;

}
