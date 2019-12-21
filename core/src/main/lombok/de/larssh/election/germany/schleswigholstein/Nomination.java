package de.larssh.election.germany.schleswigholstein;

import java.util.Optional;

public interface Nomination {
	Election getElection();

	District<?> getDistrict();

	Optional<Party> getParty();

	Person getPerson();
}
