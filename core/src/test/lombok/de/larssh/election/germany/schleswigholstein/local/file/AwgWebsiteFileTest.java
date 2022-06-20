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
 * {@link AwgWebsiteFile}
 */
@PackagePrivate
@NoArgsConstructor
class AwgWebsiteFileTest {
	/**
	 * Test writing using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testRethwisch() throws IOException {
		// given
		final LocalElectionResult result = PollingStationResultFileTest.readResultsRethwisch();
		final StringWriter writer = new StringWriter();

		// when
		AwgWebsiteFile.write(result, writer);

		// then
		assertThat(writer.toString()).isEqualTo(new String(
				Files.readAllBytes(
						Resources.getResourceRelativeTo(getClass(), Paths.get("AwgWebsiteFile-2018.php")).get()),
				Strings.DEFAULT_CHARSET));
	}
}
