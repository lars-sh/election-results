package de.larssh.election.germany.schleswigholstein.local;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.NoArgsConstructor;

/**
 * {@link LocalElection}
 */
@NoArgsConstructor
public class LocalElectionResultTest {
	@Test
	public void testJacksonForElection() throws IOException {
		final ObjectWriter objectWriter = LocalElection.createJacksonObjectWriter().withDefaultPrettyPrinter();
		final Path path = Files.createTempFile("", "");

		// Create
		final LocalElectionResult resultCreated = createElectionResult();
		objectWriter.writeValue(path.toFile(), resultCreated);

		// Open
		final LocalElectionResult resultOpened;
		try (Reader reader = Files.newBufferedReader(path)) {
			resultOpened = LocalElectionResult.fromJson(reader, LocalElectionTest.createElection());
		}

		// Compare
		final String electionCreatedToString = resultCreated.toString();
		final String electionOpenedToString = resultOpened.toString();
		assertEquals(electionCreatedToString, electionOpenedToString);
	}

	private static LocalElectionResult createElectionResult() {
		final LocalElection election = LocalElectionTest.createElection();

		final LocalPollingStation rethwischdorf = findPollingStation(election, "Rethwischdorf");
		// final LocalPollingStation kleinBoden = findPollingStation(election, "Klein
		// Boden");

		final LocalNomination poppingaJens = findNomination(election, "Poppinga", "Jens");

		final Set<LocalBallot> ballots = new HashSet<>();
		ballots.add(LocalBallot
				.createValidBallot(election, rethwischdorf, false, new HashSet<>(Arrays.asList(poppingaJens)))); // TODO

		return new LocalElectionResult(election, OptionalInt.of(20), ballots);
	}

	private static LocalNomination findNomination(final LocalElection election,
			final String familyName,
			final String givenName) {
		return election.getNominations()
				.stream()
				.filter(nomination -> familyName.equals(nomination.getPerson().getFamilyName())
						&& givenName.equals(nomination.getPerson().getGivenName()))
				.findAny()
				.get();
	}

	private static LocalPollingStation findPollingStation(final LocalElection election, final String name) {
		return election.getDistrict()
				.getChildren()
				.stream()
				.map(LocalDistrict::getChildren)
				.flatMap(Collection::stream)
				.filter(district -> name.equals(district.getName()))
				.findAny()
				.get();
	}
}
