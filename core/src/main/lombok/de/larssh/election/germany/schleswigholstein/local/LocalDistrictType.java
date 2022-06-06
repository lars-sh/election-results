package de.larssh.election.germany.schleswigholstein.local;

import de.larssh.election.germany.schleswigholstein.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Arten der Wahlgebiete
 */
@Getter
@RequiredArgsConstructor
public enum LocalDistrictType {
	/**
	 * Kreisangehörige Gemeinde
	 */
	KREISANGEHOERIGE_GEMEINDE("Kreisangehörige Gemeinde", Color.WHITE),

	/**
	 * Kreisfreie Stadt
	 */
	KREISFREIE_STADT("Kreisfreie Stadt", Color.WHITE),

	/**
	 * Kreis
	 */
	KREIS("Kreis", Color.RED);

	/**
	 * Name der Art des Wahlgebiets
	 *
	 * <p>
	 * sieeh § 15 Absatz 3 GKWG
	 */
	String name;

	/**
	 * Farbe der Stimmzettel dieser Art Wahlgebiete
	 *
	 * <p>
	 * siehe § 96 Absatz 1 GKWO
	 */
	Color colorOfBallots;

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getName();
	}
}
