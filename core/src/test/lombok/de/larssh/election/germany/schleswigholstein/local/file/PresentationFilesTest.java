package de.larssh.election.germany.schleswigholstein.local.file;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionTest;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link PresentationFiles}
 */
@PackagePrivate
@NoArgsConstructor
class PresentationFilesTest {
	/**
	 * Test writing using an empty election result
	 *
	 * <p>
	 * There is no simple way to verify the output, but at least no exception should
	 * be thrown.
	 */
	@Test
	@PackagePrivate
	void testEmpty() {
		// given
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result
				= new LocalElectionResult(election, 2, emptyMap(), emptySet(), emptySet(), emptyList());

		// when and then
		assertDoesNotThrow(() -> {
			PresentationFiles.write(result, new StringWriter());
		});
	}

	/**
	 * Test writing using results of Rethwisch
	 *
	 * <p>
	 * There is no simple way to verify the output, but at least no exception should
	 * be thrown.
	 */
	@Test
	@PackagePrivate
	void testRethwisch() {
		// given
		final LocalElectionResult result = PollingStationResultFilesTest.readResultsRethwisch();

		// when and then
		assertDoesNotThrow(() -> {
			PresentationFiles.write(result, new StringWriter());
		});
	}
}
