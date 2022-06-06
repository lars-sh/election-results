package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Finals.lazy;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.larssh.election.germany.schleswigholstein.Keys;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;
import de.larssh.utils.Optionals;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wahlvorschlag (§ 18 GKWG)
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalNomination implements Nomination<LocalNomination>, Comparable<LocalNomination> {
	/**
	 * Comparator by election, party and nomination order
	 */
	private static final Comparator<LocalNomination> COMPARATOR = Comparator.comparing(LocalNomination::getElection)
			.thenComparing(LocalNomination::getParty, Optionals.<Party>comparator())
			.thenComparing(nomination -> nomination.getElection().getNominations().indexOf(nomination));

	/**
	 * Creates a unique key for the given person and party keys.
	 *
	 * @param personKey the key of a person
	 * @param partyKey  the key of a party
	 * @return unique key for the given person and party keys
	 */
	public static String createKey(final String personKey, final Optional<String> partyKey) {
		return Keys.escape(personKey, " (", partyKey.orElse(""), ")");
	}

	/**
	 * Wahl
	 */
	@JsonIgnore
	@ToString.Exclude
	LocalElection election;

	/**
	 * Wahlkreis
	 */
	LocalDistrict district;

	/**
	 * Politische Partei, Wählergruppe oder empty für unabhängige Bewerberinnen und
	 * Bewerber
	 */
	Optional<Party> party;

	/**
	 * Bewerberin oder Bewerber
	 */
	Person person;

	/**
	 * Art des Wahlvorschlags (§ 18 Absätze 1+2 GKWG)
	 */
	@JsonIgnore
	Supplier<LocalNominationType> type = lazy(() -> !getParty().isPresent()
			|| getElection().getNominationsOfParty(getParty().get()).indexOf(this) < getElection()
					.getNumberOfDirectSeats() ? LocalNominationType.DIRECT : LocalNominationType.LIST);

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nullable final LocalNomination nomination) {
		return COMPARATOR.compare(this, nomination);
	}

	/**
	 * District as JSON property
	 *
	 * @return Wahlkreis
	 */
	@JsonProperty("district")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "JSON property")
	private String getDistrictForJackson() {
		return getDistrict().getKey();
	}

	/**
	 * Creates a unique key based on the person and party.
	 *
	 * @return unique key based on the person and party
	 */
	@JsonIgnore
	public String getKey() {
		return createKey(getPerson().getKey(), getParty().map(Party::getKey));
	}

	/**
	 * Art des Wahlvorschlags (§ 18 Absätze 1+2 GKWG)
	 *
	 * @return Art des Wahlvorschlags
	 */
	public LocalNominationType getType() {
		return type.get();
	}

	/**
	 * Party as JSON property
	 *
	 * @return Politische Partei, Wählergruppe oder empty
	 */
	@JsonProperty("party")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "JSON property")
	private Optional<String> getPartyForJackson() {
		return getParty().map(Party::getKey);
	}
}
