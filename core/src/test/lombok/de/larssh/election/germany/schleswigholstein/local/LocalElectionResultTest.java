package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFilesTest;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link LocalElectionResult}
 */
@NoArgsConstructor
@PackagePrivate
class LocalElectionResultTest {
	/**
	 * Test, that calculating an empty result does not fail
	 */
	@Test
	@PackagePrivate
	void testEmpty() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result
				= new LocalElectionResult(election, 2, emptyMap(), emptySet(), emptySet(), emptyList());

		final SoftAssertions softAssertions = new SoftAssertions();

		softAssertions.assertThat(result.getBallots()).isEmpty();
		softAssertions.assertThat(result.getEvaluationProgress(1)).isEmpty();
		softAssertions.assertThat(result.getNominationResults()).hasSameSizeAs(election.getNominations());
		result.getNominationResults().values().forEach(nominationResult -> {
			softAssertions.assertThat(nominationResult.getBallots()).isEmpty();
			softAssertions.assertThat(nominationResult.getNumberOfVotes()).isZero();
			softAssertions.assertThat(nominationResult.getSainteLagueValue()).isEmpty();
			softAssertions.assertThat(nominationResult.getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		});
		softAssertions.assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.empty());
		softAssertions.assertThat(result.getNumberOfInvalidBallots()).isZero();
		softAssertions.assertThat(result.getPartyResults()).hasSameSizeAs(election.getParties());
		result.getPartyResults().values().forEach(partyResult -> {
			softAssertions.assertThat(partyResult.getBallots()).isEmpty();
			softAssertions.assertThat(partyResult.getNumberOfBlockVotings()).isZero();
			softAssertions.assertThat(partyResult.getNumberOfVotes()).isZero();
		});

		softAssertions.assertAll();
	}

	/**
	 * Test writing and reading JSON results, creating a result equal to the
	 * original one.
	 */
	@Test
	@PackagePrivate
	void testJson() throws IOException {
		// Original result
		final LocalElectionResult originalResult = PollingStationResultFilesTest.readResultsRethwisch();

		// Write JSON
		final Path path = Files.createTempFile("", ".json");
		LocalElection.createJacksonObjectWriter().withDefaultPrettyPrinter().writeValue(path.toFile(), originalResult);

		// Read JSON
		final LocalElectionResult jsonResult;
		try (Reader reader = Files.newBufferedReader(path)) {
			jsonResult = LocalElectionResult.fromJson(reader, LocalElectionTest.createElection());
		}

		// Compare results
		assertThat(jsonResult).hasToString(originalResult.toString());
	}
}
