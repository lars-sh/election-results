package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.Set;

import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;
import de.larssh.election.germany.schleswigholstein.Nomination;
import lombok.NonNull;

public interface Buergermeisterwahlvorschlag extends Nomination {
	
	Buergermeisterwahlvorschlag NEIN = null; // TODO: Nein-Stimme

	
	Set<Party> getGruppierungen();

	
	Person getPerson();

	
	@Override
	Buergermeisterwahl getWahl();
}
