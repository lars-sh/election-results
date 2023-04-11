package de.larssh.election.germany.schleswigholstein.local;

import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;

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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalNomination implements Nomination<LocalNomination>, Comparable<LocalNomination> {
	/**
	 * Comparator by election, party and nomination order
	 */
	private static final Comparator<LocalNomination> COMPARATOR = Comparator.comparing(LocalNomination::getElection)
			.thenComparing(nomination -> nomination.getElection().getNominations().indexOf(nomination));

	/**
	 * Wahl
	 *
	 * @return Wahl
	 */
	@JsonIgnore
	@ToString.Exclude
	LocalElection election;

	/**
	 * Wahlkreis
	 *
	 * @return Wahlkreis
	 */
	@EqualsAndHashCode.Include
	LocalDistrict district;

	/**
	 * Bewerberin oder Bewerber
	 *
	 * @return Bewerberin oder Bewerber
	 */
	@EqualsAndHashCode.Include
	Person person;

	/**
	 * Politische Partei, Wählergruppe oder empty für unabhängige Bewerberinnen und
	 * Bewerber
	 *
	 * @return Politische Partei, Wählergruppe oder empty
	 */
	@EqualsAndHashCode.Include
	Optional<Party> party;

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
		return Keys.escape(getPerson().getKey(), " (", getParty().map(Party::getKey).orElse(""), ")");
	}

	/**
	 * Determines the position of the nomination on the party's list.
	 *
	 * @return the position of the nomination on the party's list or empty if the
	 *         nomination has no party assigned.
	 */
	@JsonIgnore
	public OptionalInt getListPosition() {
		return Optionals.mapToInt(getParty(), party -> getElection().getListNominations(party).indexOf(this) + 1);
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

	/**
	 * Unmittelbarer Wahlvorschlag (§ 18 Absatz 1 GKWG)
	 *
	 * @return {@code true} if direct nomination, else {@code false}
	 */
	@JsonIgnore
	public boolean isDirectNomination() {
		return getElection().getDirectNominations().contains(this);
	}

	/**
	 * Listenwahlvorschlag (§ 18 Absatz 2 GKWG)
	 *
	 * @return {@code true} if direct nomination, else {@code false}
	 */
	@JsonIgnore
	public boolean isListNomination() {
		return getElection().getListNominations().contains(this);
	}
}
