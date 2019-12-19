package de.larssh.election.germany.schleswigholstein.local;

import java.util.Comparator;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;
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
			.thenComparing(nomination -> nomination.getElection().getNominations().indexOf(nomination));

	@JsonIgnore
	LocalElection election;

	LocalDistrict district;

	LocalNominationType type;

	Person person;

	Optional<Party> party;

	@Override
	public int compareTo(@Nullable final LocalNomination nomination) {
		return COMPARATOR.compare(this, nomination);
	}

	@JsonProperty("district")
	private String getDistrictForJackson() {
		return getDistrict().getName();
	}

	@JsonProperty("party")
	private Optional<String> getPartyForJackson() {
		return getParty().map(Party::getShortName);
	}
}
