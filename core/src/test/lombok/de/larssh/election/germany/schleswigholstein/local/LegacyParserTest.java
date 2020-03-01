package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
	static LocalElectionResult readAllResults() {
		final LocalElection election = LocalElectionTest.createElection();

		final LocalElectionResult resultKleinBoden = readResult(election,
				LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN));
		final LocalElectionResult resultRethwischdorf = readResult(election,
				LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF));

		final OptionalInt numberOfAllBallots = resultKleinBoden.getNumberOfAllBallots().isPresent()
				&& resultRethwischdorf.getNumberOfAllBallots().isPresent()
						? OptionalInt.of(resultKleinBoden.getNumberOfAllBallots().getAsInt()
								+ resultRethwischdorf.getNumberOfAllBallots().getAsInt())
						: OptionalInt.empty();

		final List<LocalBallot> ballots = new ArrayList<>();
		ballots.addAll(resultKleinBoden.getBallots());
		ballots.addAll(resultRethwischdorf.getBallots());

		return new LocalElectionResult(election, numberOfAllBallots, ballots);
	}

	private static LocalElectionResult readResult(final LocalElection election,
			final LocalPollingStation pollingStation) {
		final Path path
				= Resources.getResourceRelativeTo(LegacyParserTest.class, Paths.get(pollingStation.getName() + ".txt"))
						.orElseThrow(() -> new ElectionException("Cannot find test file for polling station \"%s\".",
								pollingStation.getKey()));
		try (Reader reader = Files.newBufferedReader(path)) {
			return LegacyParser.parse(election, pollingStation, reader);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	public void testKleinBoden() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalPollingStation pollingStation
				= LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN);
		final LocalElectionResult result = readResult(election, pollingStation);

		assertThat(election.getNumberOfEligibleVoters(pollingStation)).isEqualTo(OptionalInt.of(273));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(166));
		assertThat(result.getBallots().size()).isEqualTo(191);
		assertThat(result.getCountingProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1151, 1)));
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(2);
	}

	@Test
	public void testRethwisch() {
		final LocalElectionResult result = readAllResults();
		final LocalElection election = result.getElection();

		// Compare
		assertThat(election.getNumberOfEligibleVoters()).isEqualTo(990);
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(601));
		assertThat(result.getBallots().size()).isEqualTo(627);
		assertThat(result.getCountingProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1043, 1)));
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(3);
	}

	@Test
	public void testRethwischdorf() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalPollingStation pollingStation
				= LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF);
		final LocalElectionResult result = readResult(election, pollingStation);

		assertThat(election.getNumberOfEligibleVoters(pollingStation)).isEqualTo(OptionalInt.of(717));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(435));
		assertThat(result.getBallots().size()).isEqualTo(436);
		assertThat(result.getCountingProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1002, 1)));
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(1);
	}
}
