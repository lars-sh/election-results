package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.NoArgsConstructor;

/**
 * {@link LocalElectionResult}
 */
@NoArgsConstructor
public class LocalElectionResultTest {
	@Test
	public void testEmpty() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = new LocalElectionResult(election, OptionalInt.empty(), emptyList());

		assertThat(result.getBallots()).isEmpty();
		assertThat(result.getCountingProgress(1)).isEqualTo(Optional.empty());
		assertThat(result.getNominationResults()).hasSameSizeAs(election.getNominations());
		result.getNominationResults().values().forEach(nominationResult -> {
			assertThat(nominationResult.getBallots()).isEmpty();
			assertThat(nominationResult.getSainteLagueValue()).isEqualTo(BigDecimal.ZERO);
			assertThat(nominationResult.getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		});
		assertThat(result.getNumberOfAllBallots()).isEqualTo(OptionalInt.empty());
		assertThat(result.getNumberOfInvalidBallots()).isEqualTo(0);
		assertThat(result.getPartyResults()).hasSameSizeAs(election.getParties());
		result.getPartyResults().values().forEach(partyResult -> {
			assertThat(partyResult.getBallots()).isEmpty();
			assertThat(partyResult.getNumberOfBlockVotings()).isEqualTo(0);
			assertThat(partyResult.getNumberOfVotes()).isEqualTo(0);
		});
	}

	@Test
	public void testJson() throws IOException {
		final ObjectWriter objectWriter = LocalElection.createJacksonObjectWriter().withDefaultPrettyPrinter();
		final Path path = Files.createTempFile("", "");

		// Create
		final LocalElectionResult resultCreated = LegacyParserTest.readResultsRethwisch();
		objectWriter.writeValue(path.toFile(), resultCreated);

		// Open
		final LocalElectionResult resultOpened;
		try (Reader reader = Files.newBufferedReader(path)) {
			resultOpened = LocalElectionResult.fromJson(reader, LocalElectionTest.createElection());
		}

		// Compare
		final String electionCreatedToString = resultCreated.toString();
		final String electionOpenedToString = resultOpened.toString();
		assertThat(electionOpenedToString).isEqualTo(electionCreatedToString);
	}
}
