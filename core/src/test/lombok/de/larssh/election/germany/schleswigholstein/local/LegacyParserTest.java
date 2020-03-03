package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private static LocalElectionResult readResult(final LocalElection election, final String pollingStationName) {
		return readResult(election, pollingStationName, Paths.get(pollingStationName + ".txt"));
	}

	@PackagePrivate
	static LocalElectionResult readResult(final LocalElection election,
			final String pollingStationName,
			final Path classRelativePath) {
		return readResult(election,
				LocalElectionTest.findPollingStation(election, pollingStationName),
				classRelativePath);
	}

	private static LocalElectionResult readResult(final LocalElection election,
			final LocalPollingStation pollingStation,
			final Path classRelativePath) {
		final Path path = Resources.getResourceRelativeTo(LegacyParserTest.class, classRelativePath)
				.orElseThrow(() -> new ElectionException("Cannot find test file for polling station \"%s\".",
						pollingStation.getKey()));
		try (Reader reader = Files.newBufferedReader(path)) {
			return LegacyParser.parse(election, pollingStation, reader);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@PackagePrivate
	static LocalElectionResult readResultsRethwisch() {
		final LocalElection election = LocalElectionTest.createElection();

		final LocalElectionResult resultKleinBoden
				= readResult(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN);
		final LocalElectionResult resultRethwischdorf
				= readResult(election, LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF);
		return LegacyParser.mergeResults(election, resultKleinBoden, resultRethwischdorf);
	}

	@Test
	public void testKleinBoden() {
		final LocalElection election = LocalElectionTest.createElection();
		final String pollingStationName = LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN;
		final LocalElectionResult result = readResult(election, pollingStationName);
		final LocalPollingStation pollingStation = LocalElectionTest.findPollingStation(election, pollingStationName);

		assertThat(election.getNumberOfEligibleVoters(pollingStation)).isEqualTo(OptionalInt.of(273));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(166));
		assertThat(result.getBallots().size()).isEqualTo(191);
		assertThat(result.getCountingProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1151, 1)));
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(2);
	}

	@Test
	public void testRethwisch() {
		final LocalElectionResult result = readResultsRethwisch();
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
		final String pollingStationName = LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF;
		final LocalElectionResult result = readResult(election, pollingStationName);
		final LocalPollingStation pollingStation = LocalElectionTest.findPollingStation(election, pollingStationName);

		assertThat(election.getNumberOfEligibleVoters(pollingStation)).isEqualTo(OptionalInt.of(717));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(435));
		assertThat(result.getBallots().size()).isEqualTo(436);
		assertThat(result.getCountingProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1002, 1)));
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(1);
	}
}
