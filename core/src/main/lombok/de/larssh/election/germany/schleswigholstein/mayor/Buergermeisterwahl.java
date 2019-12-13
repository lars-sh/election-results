package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.List;
import java.util.OptionalInt;

import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.utils.Either;

public interface Buergermeisterwahl extends Election {
	Buergermeisterwahlvorschlag createVorschlag();

	Either<Buergermeistermehrheitswahl, Buergermeisterstichwahl> either();

	@Override
	Buergermeisterwahlergebnis getResult(OptionalInt numberOfBallots);

	default int getNumOfStimmen() {
		return 1;
	}

	@Override
	List<Buergermeisterwahlvorschlag> getNominations();
}
