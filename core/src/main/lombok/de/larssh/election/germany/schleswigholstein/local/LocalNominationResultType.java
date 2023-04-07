package de.larssh.election.germany.schleswigholstein.local;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Arten der Vertreterinnen und Vertreter gem. § 9+10 GKWG
 *
 * <p>
 * {@link LocalNominationResult#compareTo(LocalNominationResult)} is based on
 * the order specified for the below enumeration values.
 */
@Getter
@RequiredArgsConstructor
public enum LocalNominationResultType {
	/**
	 * Direktmandat (§ 9 Absatz 5 Satz 1 GKWG)
	 */
	DIRECT(true),

	/**
	 * Mehrsitz (§ 10 Absatz 4 GKWG)
	 */
	DIRECT_BALANCE_SEAT(true),

	/**
	 * Loskandidat für Direktmandat (§ 9 Absatz 5 Satz 2 GKWG) mit sicherem
	 * Listenmandat (§ 10 Absatz 2 Satz 2 GKWG)
	 */
	DIRECT_DRAW_LIST(true),

	/**
	 * Listenmandat (§ 10 Absatz 2 Satz 2 GKWG)
	 */
	LIST(true),

	/**
	 * Überhangmandat (§ 10 Absatz 5 GKWG)
	 */
	LIST_OVERHANG_SEAT(true),

	/**
	 * Loskandidat für Direktmandat (§ 9 Absatz 5 Satz 2 GKWG)
	 */
	DIRECT_DRAW(false),

	/**
	 * Loskandidat für Listenmandat (§ 10 Absatz 2 Satz 3 GKWG)
	 */
	LIST_DRAW(false),

	/**
	 * Not elected
	 */
	NOT_ELECTED(false);

	/**
	 * Returns {@code true} if a nomination is meant to be elected. This method not
	 * only excludes {@link #NOT_ELECTED}, but also {@link #DIRECT_DRAW} and
	 * {@link #LIST_DRAW}.
	 *
	 * @return {@code true} if a nomination is meant to be elected, else
	 *         {@code false}
	 */
	boolean elected;
}
