package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.Set;

import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;

public interface Buergermeisterwahlvorschlag extends Nomination<Buergermeisterwahlvorschlag> {
	// Buergermeisterwahlvorschlag NEIN = null; // Nein-Stimme
	Set<Party> getGruppierungen();

	@Override
	Person getPerson();

	@Override
	Buergermeisterwahl getElection();
}
