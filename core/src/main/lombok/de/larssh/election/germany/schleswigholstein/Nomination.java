package de.larssh.election.germany.schleswigholstein;

import java.util.Optional;

public interface Nomination<N extends Nomination<? extends N>> {
	Election<?, ? extends N> getElection();

	District<?> getDistrict();

	Optional<Party> getParty();

	Person getPerson();
}
