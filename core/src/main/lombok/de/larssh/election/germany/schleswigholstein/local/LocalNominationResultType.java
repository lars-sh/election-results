package de.larssh.election.germany.schleswigholstein.local;

/**
 * Arten der Vertreterinnen und Vertreter gem. § 9+10 GKWG
 *
 * <p>
 * {@link LocalNominationResult#compareTo(LocalNominationResult)} is based on
 * the order specified for the below enumeration values.
 */
public enum LocalNominationResultType {
	/**
	 * Direktmandat (§ 9 Absatz 5 Satz 1 GKWG)
	 */
	DIRECT,

	/**
	 * Loskandidat für Direktmandat (§ 9 Absatz 5 Satz 2 GKWG)
	 */
	DIRECT_DRAW,

	/**
	 * Mehrsitz (§ 10 Absatz 4 GKWG)
	 */
	DIRECT_BALANCE_SEAT,

	/**
	 * Listenmandat (§ 10 Absatz 2 Satz 2 GKWG)
	 */
	LIST,

	/**
	 * Loskandidat für Listenmandat (§ 10 Absatz 2 Satz 3 GKWG)
	 */
	LIST_DRAW,

	/**
	 * Überhangmandat (§ 10 Absatz 5 GKWG)
	 */
	LIST_OVERHANG_SEAT,

	/**
	 * Not elected
	 */
	NOT_ELECTED;
}
