package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link LocalElectionResult}
 */
@NoArgsConstructor
@PackagePrivate
class LocalElectionResultTest {
	/**
	 * An empty result must not fail, but return the expected values.
	 */
	@Test
	@PackagePrivate
	void testEmpty() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result
				= new LocalElectionResult(election, OptionalInt.empty(), emptyList(), emptySet(), emptySet());

		assertThat(result.getBallots()).isEmpty();
		assertThat(result.getCountingProgress(1)).isEmpty();
		assertThat(result.getNominationResults()).hasSameSizeAs(election.getNominations());
		result.getNominationResults().values().forEach(nominationResult -> {
			assertThat(nominationResult.getBallots()).isEmpty();
			assertThat(nominationResult.getNumberOfVotes()).isZero();
			assertThat(nominationResult.getSainteLagueValue()).isEqualTo(BigDecimal.ZERO);
			assertThat(nominationResult.getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		});
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.empty());
		assertThat(result.getNumberOfInvalidBallots()).isZero();
		assertThat(result.getPartyResults()).hasSameSizeAs(election.getParties());
		result.getPartyResults().values().forEach(partyResult -> {
			assertThat(partyResult.getBallots()).isEmpty();
			assertThat(partyResult.getNumberOfBlockVotings()).isZero();
			assertThat(partyResult.getNumberOfVotes()).isZero();
		});
	}

	/**
	 * Writing and reading JSON results must create a result equal to the original
	 * one.
	 */
	@Test
	@PackagePrivate
	void testJson() throws IOException {
		// Original result
		final LocalElectionResult originalResult = LegacyParserTest.readResultsRethwisch();

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
