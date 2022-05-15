package de.larssh.election.germany.schleswigholstein.local;

/**
 * Arten der Vertreterinnen und Vertreter gem. § 9+10 KomWG SH.
 *
 * <p>
 * {@link LocalNominationResult#compareTo(LocalNominationResult)} is based on
 * the order specified for the below enumeration values.
 */
public enum LocalNominationResultType {
	/**
	 * Direktmandat (§ 9 Absatz 5 Satz 1 KomWG SH)
	 */
	DIRECT,

	/**
	 * Loskandidat für Direktmandat (§ 9 Absatz 5 Satz 2 KomWG SH)
	 */
	DIRECT_DRAW,

	/**
	 * Mehrsitz (§ 10 Absatz 4 KomWG SH)
	 */
	DIRECT_BALANCE_SEAT,

	/**
	 * Listenmandat (§ 10 Absatz 2 Satz 2 KomWG SH)
	 */
	LIST,

	/**
	 * Loskandidat für Listenmandat (§ 10 Absatz 2 Satz 3 KomWG SH)
	 */
	LIST_DRAW,

	/**
	 * Überhangmandat (§ 10 Absatz 5 KomWG SH)
	 */
	LIST_OVERHANG_SEAT,

	NOT_ELECTED;
}
