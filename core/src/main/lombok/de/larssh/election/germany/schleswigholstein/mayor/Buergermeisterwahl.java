package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.List;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.utils.Either;

public interface Buergermeisterwahl extends Election<District<?>, Buergermeisterwahlvorschlag> {
	Buergermeisterwahlvorschlag createVorschlag();

	Either<Buergermeistermehrheitswahl, Buergermeisterstichwahl> either();

	default int getNumOfStimmen() {
		return 1;
	}

	@Override
	List<Buergermeisterwahlvorschlag> getNominations();
}
