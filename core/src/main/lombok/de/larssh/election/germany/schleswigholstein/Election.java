package de.larssh.election.germany.schleswigholstein;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Wahlen in den Gemeinden und Kreisen in Schleswig-Holstein gem. Gemeinde- und
 * Kreiswahlgesetz (GKWG)
 *
 * @param <D> the type of the root district
 * @param <N> the type of nominations
 */
public interface Election<D extends District<?>, N extends Nomination<? extends N>> extends Comparable<Election<?, ?>> {
	/**
	 * Comparator by district, name and date
	 */
	Comparator<Election<?, ?>> COMPARATOR = Comparator.<Election<?, ?>, District<?>>comparing(Election::getDistrict)
			.thenComparing(Election::getName)
			.thenComparing(Election::getDate);

	/**
	 * Wahlgebiet (§ 2 GKWG)
	 *
	 * @return Wahlgebiet
	 */
	D getDistrict();

	/**
	 * Date of the election
	 *
	 * @return the date of the election
	 */
	LocalDate getDate();

	/**
	 * Name of the election
	 *
	 * @return the name of the election
	 */
	String getName();

	/**
	 * Einwohnerzahl des Wahlgebiets
	 *
	 * @return Einwohnerzahl des Wahlgebiets
	 */
	default int getPopulation() {
		return getPopulation(getDistrict())
				.orElseThrow(() -> new ElectionException("Missing population information for root district \"%s\".",
						getDistrict().getName()));
	}

	/**
	 * Einwohnerzahl nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 *
	 * @param district Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @return Einwohnerzahl nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 */
	OptionalInt getPopulation(District<?> district);

	/**
	 * Sets population information for {@code district}.
	 *
	 * <p>
	 * Population information, that's already been set cannot be overwritten and
	 * {@code district} must be part of elections district hierarchy.
	 *
	 * @param district   Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @param population Einwohnerzahl
	 */
	default void setPopulation(final District<?> district, final int population) {
		setPopulation(district, OptionalInt.of(population));
	}

	/**
	 * Sets population information for {@code district}.
	 *
	 * <p>
	 * Population information, that's already been set cannot be overwritten and
	 * {@code district} must be part of elections district hierarchy.
	 *
	 * @param district   Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @param population Einwohnerzahl
	 */
	void setPopulation(District<?> district, OptionalInt population);

	/**
	 * Anzahl der Wahlberechtigten im Wahlgebiet
	 *
	 * @return Anzahl der Wahlberechtigten im Wahlgebiet
	 */
	default OptionalInt getNumberOfEligibleVoters() {
		return getNumberOfEligibleVoters(getDistrict());
	}

	/**
	 * Anzahl der Wahlberechtigten nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 *
	 * @param district Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @return Anzahl der Wahlberechtigten nach Wahlgebiet, Wahlkreis oder
	 *         Wahlbezirk
	 */
	OptionalInt getNumberOfEligibleVoters(District<?> district);

	/**
	 * Sets the number of eligible voters for {@code district}.
	 *
	 * <p>
	 * The number of eligible voters, that's already been set cannot be overwritten
	 * and {@code district} must be part of elections district hierarchy.
	 *
	 * @param district               Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @param numberOfEligibleVoters Anzahl der Wahlberechtigten
	 */
	default void setNumberOfEligibleVoters(final District<?> district, final int numberOfEligibleVoters) {
		setNumberOfEligibleVoters(district, OptionalInt.of(numberOfEligibleVoters));
	}

	/**
	 * Sets the number of eligible voters for {@code district}.
	 *
	 * <p>
	 * The number of eligible voters, that's already been set cannot be overwritten
	 * and {@code district} must be part of elections district hierarchy.
	 *
	 * @param district               Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @param numberOfEligibleVoters Anzahl der Wahlberechtigten
	 */
	void setNumberOfEligibleVoters(District<?> district, OptionalInt numberOfEligibleVoters);

	/**
	 * Bewerberinnen und Bewerber
	 *
	 * @return Bewerberinnen und Bewerber
	 */
	Set<? extends N> getNominations();

	/** {@inheritDoc} */
	@Override
	default int compareTo(@Nullable final Election<?, ?> election) {
		return COMPARATOR.compare(this, election);
	}
}
