package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;
import java.util.Set;

import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;
import de.larssh.utils.Either;

public interface LocalNomination extends Nomination {
	Either<LocalNominationDirect, LocalListNomination> either();

	@Override
	LocalElection getWahl();

	public interface LocalListNomination extends LocalNomination {
		@Override
		default Either<LocalNominationDirect, LocalListNomination> either() {
			return Either.ofSecond(this);
		}

		Party getGruppierung();

		Set<Person> getPersonen();
	}

	public interface LocalNominationDirect extends LocalNomination {
		@Override
		default Either<LocalNominationDirect, LocalListNomination> either() {
			return Either.ofFirst(this);
		}

		Optional<Party> getGruppierung();

		Person getPerson();

		Wahlkreis getWahlkreis();
	}
}
