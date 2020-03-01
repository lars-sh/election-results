package de.larssh.election.germany.schleswigholstein.local;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.NoArgsConstructor;

/**
 * {@link LocalElectionResult}
 */
@NoArgsConstructor
public class LocalElectionResultTest {
	@Test
	public void testJson() throws IOException {
		final ObjectWriter objectWriter = LocalElection.createJacksonObjectWriter().withDefaultPrettyPrinter();
		final Path path = Files.createTempFile("", "");

		// Create
		final LocalElectionResult resultCreated = LegacyParserTest.readAllResults();
		objectWriter.writeValue(path.toFile(), resultCreated);

		// Open
		final LocalElectionResult resultOpened;
		try (Reader reader = Files.newBufferedReader(path)) {
			resultOpened = LocalElectionResult.fromJson(reader, LocalElectionTest.createElection());
		}

		// Compare
		// final String electionCreatedToString = resultCreated.toString();
		// final String electionOpenedToString = resultOpened.toString();
		// TODO: assertEquals(electionCreatedToString, electionOpenedToString);
	}

	@Test
	public void testResultType() {
		// TODO: Result Type
	}

	@Test
	public void testSainteLague() {
		// TODO: Sainte Lague
	}
}
