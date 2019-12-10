package de.larssh.election.germany.schleswigholstein.local;

import java.awt.Color;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocalDistrictType {
	/**
	 * Kreisangehörige Gemeinde
	 */
	KREISANGEHOERIGE_GEMEINDE(Color.WHITE),

	/**
	 * Kreisfreie Stadt
	 */
	KREISFREIE_STADT(Color.WHITE),

	/**
	 * Kreis
	 */
	KREIS(Color.RED);

	Color colorOfBallots;
}
