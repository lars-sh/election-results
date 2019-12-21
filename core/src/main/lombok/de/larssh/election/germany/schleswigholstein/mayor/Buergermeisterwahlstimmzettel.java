package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.larssh.election.germany.schleswigholstein.Ballot;

public interface Buergermeisterwahlstimmzettel extends Ballot {
	Buergermeisterwahlvorschlag getVorschlag();

	@Override
	default Set<Buergermeisterwahlvorschlag> getNominations() {
		return new HashSet<>(Arrays.asList(getVorschlag()));
	}

	@Override
	Buergermeisterwahl getElection();
}