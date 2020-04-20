package de.larssh.election.germany.schleswigholstein.local;

/**
 * {@link LocalNominationResult#compareTo(LocalNominationResult)} is based on
 * the order specified for the below enumeration values.
 */
public enum LocalNominationResultType {
	NOT_ELECTED,

	/**
	 * Direktmandat
	 */
	DIRECT,

	/**
	 * Loskandidat für Direktmandat
	 */
	DIRECT_DRAW,

	/**
	 * Mehrsitz
	 */
	DIRECT_BALANCE_SEAT,

	/**
	 * Listenmandat
	 */
	LIST,

	/**
	 * Loskandidat für Listenmandat
	 */
	LIST_DRAW,

	/**
	 * Überhangmandat
	 */
	LIST_OVERHANG_SEAT;
}
