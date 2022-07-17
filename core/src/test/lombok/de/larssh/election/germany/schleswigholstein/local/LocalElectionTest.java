package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectWriter;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.utils.Finals;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.io.Resources;
import lombok.NoArgsConstructor;

/**
 * {@link LocalElection}
 */
@NoArgsConstructor
public class LocalElectionTest {
	/**
	 * Name des Wahlbezirks "Klein Boden"
	 */
	public static final String POLLING_STATION_NAME_KLEIN_BODEN = Finals.constant("Klein Boden");

	/**
	 * Name des Wahlbezirks "Rethwischdorf"
	 */
	public static final String POLLING_STATION_NAME_RETHWISCHDORF = Finals.constant("Rethwischdorf");

	/**
	 * Creates an election for testing purposes.
	 *
	 * <p>
	 * To simplify verifying results the test case is based on the Kommunalwahl 2018
	 * in Rethwisch.
	 *
	 * @return Wahl
	 */
	public static LocalElection createElection() {
		try (Reader reader = Files.newBufferedReader(
				Resources
						.getResourceRelativeTo(LocalElectionTest.class,
								Paths.get(LocalElection.class.getSimpleName() + ".json"))
						.get())) {
			return LocalElection.fromJson(reader);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Finds a nomination based the family and given name.
	 *
	 * <p>
	 * This method throws an {@link ElectionException} in case no such nomination
	 * can be found.
	 *
	 * @param election   the election to search
	 * @param familyName the nomination's family name
	 * @param givenName  the nomination's given name
	 * @return the nomination
	 */
	@PackagePrivate
	static LocalNomination findNomination(final LocalElection election,
			final String familyName,
			final String givenName) {
		return election.getNominations()
				.stream()
				.filter(nomination -> familyName.equals(nomination.getPerson().getFamilyName())
						&& givenName.equals(nomination.getPerson().getGivenName()))
				.findAny()
				.orElseThrow(() -> new ElectionException("Cannot find a nomination named \"%s, %s\".",
						familyName,
						givenName));
	}

	/**
	 * Finds a party based its short name.
	 *
	 * <p>
	 * This method throws an {@link ElectionException} in case no such party can be
	 * found.
	 *
	 * @param election  the election to search
	 * @param shortName the party's short name
	 * @return the party
	 */
	@PackagePrivate
	static Party findParty(final LocalElection election, final String shortName) {
		return election.getParties()
				.stream()
				.filter(party -> shortName.equals(party.getShortName()))
				.findAny()
				.orElseThrow(() -> new ElectionException("Cannot find a party with short name \"%s\".", shortName));
	}

	/**
	 * Finds a polling station based its name.
	 *
	 * <p>
	 * This method throws an {@link ElectionException} in case no such polling
	 * station can be found.
	 *
	 * @param election the election to search
	 * @param name     the polling station's name
	 * @return the polling station
	 */
	public static LocalPollingStation findPollingStation(final LocalElection election, final String name) {
		return election.getDistrict()
				.getChildren()
				.stream()
				.map(LocalDistrict::getChildren)
				.flatMap(Collection::stream)
				.filter(district -> name.equals(district.getName()))
				.findAny()
				.orElseThrow(() -> new ElectionException("Cannot find a polling station named \"%s\".", name));
	}

	/**
	 * Test writing and reading JSON, creating an election equal to the original
	 * one.
	 */
	@Test
	@PackagePrivate
	void testJson() throws IOException {
		final ObjectWriter objectWriter = LocalElection.createJacksonObjectWriter().withDefaultPrettyPrinter();
		final Path path = Files.createTempFile("", "");

		// Create
		final LocalElection electionCreated = createElection();
		objectWriter.writeValue(path.toFile(), electionCreated);

		// Open
		final LocalElection electionOpened;
		try (Reader reader = Files.newBufferedReader(path)) {
			electionOpened = LocalElection.fromJson(reader);
		}

		// Compare
		final String electionCreatedToString = electionCreated.toString();
		final String electionOpenedToString = electionOpened.toString();
		assertThat(electionOpenedToString).isEqualTo(electionCreatedToString);
	}
}
