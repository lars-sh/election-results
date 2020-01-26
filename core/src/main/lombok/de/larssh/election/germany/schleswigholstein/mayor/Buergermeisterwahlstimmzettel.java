package de.larssh.election.germany.schleswigholstein.mayor;

import static java.util.Collections.singleton;

import java.util.Set;

import de.larssh.election.germany.schleswigholstein.Ballot;

public interface Buergermeisterwahlstimmzettel extends Ballot {
	Buergermeisterwahlvorschlag getVorschlag();

	@Override
	default Set<Buergermeisterwahlvorschlag> getNominations() {
		return singleton(getVorschlag());
	}

	@Override
	Buergermeisterwahl getElection();
}
