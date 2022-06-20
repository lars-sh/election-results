package de.larssh.election.germany.schleswigholstein.local.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.io.Resources;
import de.larssh.utils.text.Strings;
import lombok.NoArgsConstructor;

/**
 * {@link AwgWebsiteFiles}
 */
@PackagePrivate
@NoArgsConstructor
class AwgWebsiteFilesTest {
	/**
	 * Test writing using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testRethwisch() throws IOException {
		// given
		final LocalElectionResult result = PollingStationResultFilesTest.readResultsRethwisch();
		final StringWriter writer = new StringWriter();

		// when
		AwgWebsiteFiles.write(result, writer);

		// then
		final String expectedString = new String(Files.readAllBytes(Resources
				.getResourceRelativeTo(getClass(), Paths.get(AwgWebsiteFiles.class.getSimpleName() + "-2018.php"))
				.get()), Strings.DEFAULT_CHARSET);
		assertThat(writer.toString()).isEqualTo(expectedString);
	}
}
