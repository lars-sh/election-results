package de.larssh.election.germany.schleswigholstein.local;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

	String name;

	Color colorOfBallots;

	@Override
	public String toString() {
		return getName();
	}
}
