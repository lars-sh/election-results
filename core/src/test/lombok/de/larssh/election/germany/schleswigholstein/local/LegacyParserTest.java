package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.utils.Resources;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link LegacyParser}
 */
@NoArgsConstructor
public class LegacyParserTest {
	@PackagePrivate
	LocalElectionResult readResultFromResource(final LocalElection election, final LocalPollingStation pollingStation) {
		final Path path = Resources.getResourceRelativeTo(getClass(), Paths.get(pollingStation.getName() + ".txt"))
				.orElseThrow(() -> new ElectionException("Cannot find test file for polling station \"%s\".",
						pollingStation.getKey()));
		try (Reader reader = Files.newBufferedReader(path)) {
			return LegacyParser.parse(election, pollingStation, reader);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	// TODO: Read all results in a second test case!

	/**
	 * {@link LegacyParser#parse(LocalElection, LocalPollingStation, Reader)}
	 */
	@Test
	public void testParseKleinBoden() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalPollingStation pollingStation
				= LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN);
		final LocalElectionResult result = readResultFromResource(election, pollingStation);

		// Compare
		assertThat(election.getNumberOfEligibleVoters(pollingStation)).isEqualTo(OptionalInt.of(273));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(166));
		assertThat(LocalElectionResult.calculateVoterParticipation(election.getNumberOfEligibleVoters(pollingStation)
				.getAsInt(), result.getNumberOfAllBallots().getAsInt(), 1)).isEqualTo(BigDecimal.valueOf(608, 1));
		assertThat(result.getBallots().size()).isEqualTo(191); // TODO
		assertThat(result.getCountingProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1151, 1))); // TODO
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(2);

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

		// TODO: Blockstimmen

		// TODO: Gruppierungen
	}

	/**
	 * {@link LegacyParser#parse(LocalElection, LocalPollingStation, Reader)}
	 */
	@Test
	public void testParseRethwischdorf() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalPollingStation pollingStation
				= LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF);
		final LocalElectionResult result = readResultFromResource(election, pollingStation);

		// Compare
		assertThat(election.getNumberOfEligibleVoters(pollingStation)).isEqualTo(OptionalInt.of(717));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(435));
		assertThat(LocalElectionResult.calculateVoterParticipation(election.getNumberOfEligibleVoters(pollingStation)
				.getAsInt(), result.getNumberOfAllBallots().getAsInt(), 1)).isEqualTo(BigDecimal.valueOf(607, 1));
		assertThat(result.getBallots().size()).isEqualTo(436); // TODO
		assertThat(result.getCountingProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1002, 1))); // TODO
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(1);

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
