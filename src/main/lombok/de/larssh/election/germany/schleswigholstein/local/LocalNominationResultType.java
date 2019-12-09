package de.larssh.election.germany.schleswigholstein.local;

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
