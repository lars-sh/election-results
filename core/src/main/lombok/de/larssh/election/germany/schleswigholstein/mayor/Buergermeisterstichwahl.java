package de.larssh.election.germany.schleswigholstein.mayor;

import de.larssh.utils.Either;

public interface Buergermeisterstichwahl extends Buergermeisterwahl {
	@Override
	default Either<Buergermeistermehrheitswahl, Buergermeisterstichwahl> either() {
		return Either.ofSecond(this);
	}
}
