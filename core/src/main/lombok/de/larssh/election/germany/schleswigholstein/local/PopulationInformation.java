package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.emptyNavigableMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.utils.collection.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopulationInformation {
	private static final Map<LocalDistrictType, PopulationInformation> INSTANCES
			= Maps.<LocalDistrictType, PopulationInformation>builder()
					.put(LocalDistrictType.KREIS, createKreis())
					.put(LocalDistrictType.KREISFREIE_STADT, createKreisfreieStadt())
					.put(LocalDistrictType.KREISANGEHOERIGE_GEMEINDE, createKreisangehoerigeGemeinde())
					.unmodifiable();

	/**
	 * siehe § 8 Absatz 3 KomWG SH und § 9 Absatz 3 KomWG SH
	 */
	@SuppressWarnings("checkstyle:MagicNumber")
	private static PopulationInformation createKreis() {
		return new PopulationInformation(LocalDistrictType.KREIS,
				emptyNavigableMap(),
				(NavigableMap<Integer, Integer>) Maps.builder(new TreeMap<Integer, Integer>()) //
						.put(0, 23)
						.put(200_000, 25)
						.get());
	}

	/**
	 * siehe § 8 Absatz 1 KomWG SH und § 9 Absätze 1-2 KomWG SH
	 */
	@SuppressWarnings("checkstyle:MagicNumber")
	private static PopulationInformation createKreisangehoerigeGemeinde() {
		return new PopulationInformation(LocalDistrictType.KREIS,
				(NavigableMap<Integer, Integer>) Maps.builder(new TreeMap<Integer, Integer>()) //
						.put(2500, 1)
						.put(5000, 2)
						.put(10_000, 3)
						.get(),
				(NavigableMap<Integer, Integer>) Maps.builder(new TreeMap<Integer, Integer>()) //
						.put(70, 4)
						.put(200, 5)
						.put(750, 6)
						.put(1250, 7)
						.put(2500, 9)
						.put(5000, 10)
						.put(10_000, 12)
						.put(15_000, 14)
						.put(25_000, 16)
						.put(35_000, 18)
						.put(45_000, 20)
						.get());
	}

	/**
	 * siehe § 8 Absatz 2 KomWG SH und § 9 Absatz 3 KomWG SH
	 */
	@SuppressWarnings("checkstyle:MagicNumber")
	private static PopulationInformation createKreisfreieStadt() {
		return new PopulationInformation(LocalDistrictType.KREISFREIE_STADT,
				emptyNavigableMap(),
				(NavigableMap<Integer, Integer>) Maps.builder(new TreeMap<Integer, Integer>()) //
						.put(0, 22)
						.put(150_000, 25)
						.get());
	}

	public static PopulationInformation get(final LocalDistrictType type) {
		return INSTANCES.get(type);
	}

	@Getter
	LocalDistrictType type;

	/**
	 * Anzahl der Wahlkreise nach maximaler Einwohnerzahl (§ 9 Absätze 1-3 KomWG SH)
	 *
	 * <p>
	 * Bei mehr Einwohnern gilt {@link #numberOfDirectSeatsIfPopulationIsGreater}.
	 */
	NavigableMap<Integer, Integer> numberOfDistrictsIfPopulationIsLessOrEqual;

	/**
	 * Anzahl der unmittelbaren Vertreterinnen und Vertreter nach minimaler
	 * Einwohnerzahl (§ 8 KomWG SH)
	 */
	NavigableMap<Integer, Integer> numberOfDirectSeatsIfPopulationIsGreater;

	public int getNumberOfDistricts(final int population) {
		final Entry<Integer, Integer> entry = numberOfDistrictsIfPopulationIsLessOrEqual.higherEntry(population);
		return entry == null ? getNumberOfDirectSeats(population) : entry.getValue();
	}

	public int getNumberOfDirectSeats(final int population) {
		final Entry<Integer, Integer> entry = numberOfDirectSeatsIfPopulationIsGreater.lowerEntry(population);
		if (entry == null) {
			throw new ElectionException(
					"Population %d is too low to calculate the number of direct seats for. Increase population to %d to calculate the number of direct seats.",
					population,
					numberOfDirectSeatsIfPopulationIsGreater.firstKey());
		}
		return entry.getValue();
	}
}
