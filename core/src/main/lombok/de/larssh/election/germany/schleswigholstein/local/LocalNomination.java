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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(onParam_ = { @Nullable })
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalNomination implements Nomination, Comparable<LocalNomination> {
	private static final Comparator<LocalNomination> COMPARATOR = Comparator.comparing(LocalNomination::getElection)
			.thenComparing(LocalNomination::getParty, Optionals.comparator())
			.thenComparing(nomination -> nomination.getElection().getNominations().indexOf(nomination));

	@JsonIgnore
	@ToString.Exclude
	LocalElection election;

	LocalDistrict district;

	Optional<Party> party;

	Person person;

	@JsonIgnore
	Supplier<LocalNominationType> type = lazy(() -> !getParty().isPresent()
			|| getElection().getNominationsOfParty(getParty().get()).indexOf(this) < getElection()
					.getNumberOfDirectSeats() ? LocalNominationType.DIRECT : LocalNominationType.LIST);

	@Override
	public int compareTo(@Nullable final LocalNomination nomination) {
		return COMPARATOR.compare(this, nomination);
	}

	@JsonProperty("district")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private String getDistrictForJackson() {
		return getDistrict().getKey();
	}

	@JsonIgnore
	public String getKey() {
		return String.format("%s, %s%s",
				Keys.escape(getPerson().getFamilyName(), ',', ' '),
				Keys.escape(getPerson().getGivenName(), ' ', '('),
				getParty().map(Party::getKey).map(key -> " (" + key + ')').orElse(""));
	}

	public LocalNominationType getType() {
		return type.get();
	}

	@JsonProperty("party")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private Optional<String> getPartyForJackson() {
		return getParty().map(Party::getKey);
	}
}
