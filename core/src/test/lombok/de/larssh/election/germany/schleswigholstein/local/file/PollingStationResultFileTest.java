package de.larssh.election.germany.schleswigholstein.local.file;

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
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionTest;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFile;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.io.Resources;
import lombok.NoArgsConstructor;

/**
 * {@link PollingStationResultFile}
 */
@NoArgsConstructor
public class PollingStationResultFileTest {
	/**
	 * Read the result file of a {@code pollingStationName} from a class-relative
	 * text file
	 *
	 * @param election           the election to reference
	 * @param pollingStationName the name of a polling station
	 * @return the {@link LocalElectionResult}
	 */
	private static LocalElectionResult readResult(final LocalElection election, final String pollingStationName) {
		return readResult(election,
				pollingStationName,
				Paths.get("PollingStationResultFile-" + pollingStationName + ".txt"));
	}

	/**
	 * Read the result file of {@code classRelativePath}
	 *
	 * @param election           the election to reference
	 * @param pollingStationName the name of a polling station
	 * @param classRelativePath  path to a class-relative text file
	 * @return the {@link LocalElectionResult}
	 */
	public static LocalElectionResult readResult(final LocalElection election,
			final String pollingStationName,
			final Path classRelativePath) {
		final LocalPollingStation pollingStation = LocalElectionTest.findPollingStation(election, pollingStationName);
		final Path path = Resources.getResourceRelativeTo(PollingStationResultFileTest.class, classRelativePath)
				.orElseThrow(() -> new ElectionException("Cannot find test file for polling station \"%s\".",
						pollingStation.getKey()));
		try (Reader reader = Files.newBufferedReader(path)) {
			return PollingStationResultFile.read(election, pollingStation, reader);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Read one result containing all data of Rethwisch
	 *
	 * @return one result containing all data of Rethwisch
	 */
	public static LocalElectionResult readResultsRethwisch() {
		final LocalElection election = LocalElectionTest.createElection();

		final LocalElectionResult resultKleinBoden
				= readResult(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN);
		final LocalElectionResult resultRethwischdorf
				= readResult(election, LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF);
		return resultKleinBoden.add(resultRethwischdorf);
	}

	/**
	 * Test parsing using results of Klein Boden
	 */
	@Test
	@PackagePrivate
	void testKleinBoden() {
		final LocalElection election = LocalElectionTest.createElection();
		final String pollingStationName = LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN;
		final LocalElectionResult result = readResult(election, pollingStationName);
		final LocalPollingStation pollingStation = LocalElectionTest.findPollingStation(election, pollingStationName);

		assertThat(election.getNumberOfEligibleVoters(pollingStation)).isEqualTo(OptionalInt.of(273));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(166));
		assertThat(result.getBallots()).hasSize(191);
		assertThat(result.getEvaluationProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1151, 1)));
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(2);
	}

	/**
	 * Test parsing using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testRethwisch() {
		final LocalElectionResult result = readResultsRethwisch();
		final LocalElection election = result.getElection();

		// Compare
		assertThat(election.getNumberOfEligibleVoters()).isEqualTo(OptionalInt.of(990));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(601));
		assertThat(result.getBallots()).hasSize(627);
		assertThat(result.getEvaluationProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1043, 1)));
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(3);
	}

	/**
	 * Test parsing using results of Rethwischdorf
	 */
	@Test
	@PackagePrivate
	void testRethwischdorf() {
		final LocalElection election = LocalElectionTest.createElection();
		final String pollingStationName = LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF;
		final LocalElectionResult result = readResult(election, pollingStationName);
		final LocalPollingStation pollingStation = LocalElectionTest.findPollingStation(election, pollingStationName);

		assertThat(election.getNumberOfEligibleVoters(pollingStation)).isEqualTo(OptionalInt.of(717));
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.of(435));
		assertThat(result.getBallots()).hasSize(436);
		assertThat(result.getEvaluationProgress(1)).isEqualTo(Optional.of(BigDecimal.valueOf(1002, 1)));
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(1);
	}
}
