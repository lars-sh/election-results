package de.larssh.election.germany.schleswigholstein.local;

public enum LocalNominationResultType {
	NOT_ELECTED,

	/**
	 * Direktmandat
	 */
	DIRECT,

	/**
	 * TODO: Loskandidat
	 */
	DRAW,

	/**
	 * Listenmandat
	 */
	LIST,

	/**
	 * Überhangmandat
	 */
	OVERHANG_SEAT,

	/**
	 * Mehrsitz
	 */
	BALANCE_SEAT;
}
