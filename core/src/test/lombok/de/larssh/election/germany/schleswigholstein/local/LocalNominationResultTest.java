package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.NoArgsConstructor;

/**
 * {@link LegacyParser}
 */
@NoArgsConstructor
public class LocalNominationResultTest {
	@Test
	public void testKleinBoden() {
		final LocalElectionResult result = LegacyParserTest.readAllResults()
				.filter(ballot -> ballot.getPollingStation()
						.getName()
						.equals(LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN));
		final LocalElection election = result.getElection();

		// @formatter:off
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getBallots().size()).isEqualTo(106);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getBallots().size()).isEqualTo(71);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getBallots().size()).isEqualTo(69);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getBallots().size()).isEqualTo(64);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getBallots().size()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getBallots().size()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getBallots().size()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getBallots().size()).isEqualTo(46);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getBallots().size()).isEqualTo(41);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getBallots().size()).isEqualTo(37);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getBallots().size()).isEqualTo(34);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getBallots().size()).isEqualTo(29);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getBallots().size()).isEqualTo(28);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getBallots().size()).isEqualTo(27);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getBallots().size()).isEqualTo(26);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getBallots().size()).isEqualTo(24);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getBallots().size()).isEqualTo(20);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getBallots().size()).isEqualTo(18);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getBallots().size()).isEqualTo(16);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getBallots().size()).isEqualTo(14);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getBallots().size()).isEqualTo(12);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getBallots().size()).isEqualTo(11);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getBallots().size()).isEqualTo(10);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getBallots().size()).isEqualTo(10);
		// @formatter:on
	}

	@Test
	public void testRethwisch() {
		// TODO: Nominations Rethwisch
	}

	@Test
	public void testRethwischdorf() {
		final LocalElectionResult result = LegacyParserTest.readAllResults()
				.filter(ballot -> ballot.getPollingStation()
						.getName()
						.equals(LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF));
		final LocalElection election = result.getElection();

		// @formatter:off
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getBallots().size()).isEqualTo(222);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getBallots().size()).isEqualTo(169);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getBallots().size()).isEqualTo(151);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getBallots().size()).isEqualTo(148);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getBallots().size()).isEqualTo(136);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getBallots().size()).isEqualTo(134);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getBallots().size()).isEqualTo(133);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getBallots().size()).isEqualTo(126);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getBallots().size()).isEqualTo(124);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getBallots().size()).isEqualTo(109);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getBallots().size()).isEqualTo(105);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getBallots().size()).isEqualTo(84);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getBallots().size()).isEqualTo(82);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getBallots().size()).isEqualTo(82);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getBallots().size()).isEqualTo(73);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getBallots().size()).isEqualTo(69);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getBallots().size()).isEqualTo(68);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getBallots().size()).isEqualTo(63);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getBallots().size()).isEqualTo(58);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getBallots().size()).isEqualTo(52);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getBallots().size()).isEqualTo(48);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getBallots().size()).isEqualTo(46);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getBallots().size()).isEqualTo(33);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getBallots().size()).isEqualTo(27);
		// @formatter:on
	}
}
