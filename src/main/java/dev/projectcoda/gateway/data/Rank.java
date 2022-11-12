/*
 * Copyright (C) Gerard Sayson, 2022.
 * All rights reserved.
 */

package dev.projectcoda.gateway.data;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

/**
 * Represents the matchmaking rank of the user. The ranks are listed from the highest to lowest.
 * <ul>
 *     <li><b>S+</b> at <em>4500 rating</em>;</li>
 *     <li><b>S</b> at <em>3500 rating</em>;</li>
 *     <li><b>A</b> at <em>2000 rating</em>;</li>
 *     <li><b>B</b> at <em>1600 rating</em>;</li>
 *     <li><b>C</b> at <em>1200 rating</em>;</li>
 *     <li><b>D</b> at <em>1000 rating</em>;</li>
 *     <li><b>E</b> at <em>750 rating</em>;</li>
 *     <li><b>F</b> at <em>749 rating</em> and under.</li>
 * </ul>
 * A player's rating is never lower than zero.
 * @author Gerard Sayson
 */

@Getter
public enum Rank {

	SP("S+", 4500),
	S("S", 3500),
	A("A", 2000),
	B("B", 1600),
	C("C", 1200),
	D("D", 2000),
	E("E", 750),
	F("F", 0), // rating <= 749; how would people get this?!
	UNRANKED("Unranked", 0) // player needs 10 games played minimum to be ranked.
	;

	/**
	 * The official name of the rank.
	 * @see Rank
	 */
	private final String name;

	/**
	 * The rating required to reach the rank (inclusive).
	 */
	private final @PositiveOrZero int threshold;

	/**
	 * The enum constructor for all enum constants.
	 * @param name The official name of the rank.
	 * @param threshold The rating required to obtain the rank.
	 */
	Rank(@NotBlank String name, @PositiveOrZero int threshold) {
		this.name = name;
		this.threshold = threshold;
	}

	/**
	 * Evaluates the rank of a player from the given rating.
	 * @param rating The rating to evaluate.
	 * @return the {@link Rank} enum constant whose threshold is equal to or less than the given rating.
	 */
	@SuppressWarnings("unused")
	public static Rank fromRating(@PositiveOrZero int rating) {
		for(Rank rank : Rank.values()) {
			if(rating >= rank.threshold) return rank;
		}
		// impossible to get here, rating >= zero and F >= 0
		throw new InternalError("unreachable");
	}

	@Override
	public String toString() {
		return name;
	}
}
