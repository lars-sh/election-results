package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectWriter;

import de.larssh.election.germany.schleswigholstein.Color;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.Gender;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyType;
import de.larssh.election.germany.schleswigholstein.Person;
import de.larssh.utils.Finals;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link LocalElection}
 */
@NoArgsConstructor
public class LocalElectionTest {
	/**
	 * Name des Wahlbezirks "Klein Boden"
	 */
	@PackagePrivate
	static final String POLLING_STATION_NAME_KLEIN_BODEN = Finals.constant("Klein Boden");

	/**
	 * Name des Wahlbezirks "Rethwischdorf"
	 */
	@PackagePrivate
	static final String POLLING_STATION_NAME_RETHWISCHDORF = Finals.constant("Rethwischdorf");

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
		final LocalDistrictRoot districtRoot
				= new LocalDistrictRoot("Gemeinde Rethwisch", LocalDistrictType.KREISANGEHOERIGE_GEMEINDE);

		final LocalElection election = new LocalElection(districtRoot, LocalDate.of(2018, 5, 6), "Gemeindewahl", 2);
		election.setPopulation(districtRoot, OptionalInt.empty());
		election.setNumberOfEligibleVoters(districtRoot, OptionalInt.empty());

		// Add districts first, because nominations are based on the districts
		addDistricts(election);
		addPartiesAndNominations(election);

		return election;
	}

	/**
	 * Adds districts for testing to {@code election}.
	 *
	 * @param election Wahl
	 */
	private static void addDistricts(final LocalElection election) {
		final LocalDistrict districtLocal = election.getDistrict().createChild("Rethwisch");
		election.setPopulation(districtLocal, 1186);
		election.setNumberOfEligibleVoters(districtLocal, OptionalInt.empty());

		final LocalPollingStation pollingStationRethwischdorf
				= districtLocal.createChild(POLLING_STATION_NAME_RETHWISCHDORF);
		election.setPopulation(pollingStationRethwischdorf, OptionalInt.empty());
		election.setNumberOfEligibleVoters(pollingStationRethwischdorf, 717);

		final LocalPollingStation pollingStationKleinBoden
				= districtLocal.createChild(POLLING_STATION_NAME_KLEIN_BODEN);
		election.setPopulation(pollingStationKleinBoden, OptionalInt.empty());
		election.setNumberOfEligibleVoters(pollingStationKleinBoden, 273);
	}

	/**
	 * Adds parties and nominations for testing to {@code election}.
	 *
	 * @param election Wahl
	 */
	private static void addPartiesAndNominations(final LocalElection election) {
		final Party partyCdu = new Party("CDU",
				"Christlich Demokratische Union Deutschlands, Ortsverband Rethwisch",
				PartyType.POLITICAL_PARTY,
				Color.BLACK,
				Color.WHITE,
				Optional.empty());
		final Party partySpd = new Party("SPD",
				"Sozialdemokratische Partei Deutschlands, Ortsverband Rethwisch",
				PartyType.POLITICAL_PARTY,
				Color.rgb(237, 28, 36),
				Color.WHITE,
				Optional.empty());
		final Party partyAwg = new Party("AWG",
				"Allgemeine Wählergemeinschaft Rethwisch",
				PartyType.ASSOCIATION_OF_VOTERS,
				Color.rgb(255, 204, 0),
				Color.BLACK,
				Optional.empty());
		final Party partyFwr = new Party("FWR",
				"Freie Wählergemeinschaft Rethwisch",
				PartyType.ASSOCIATION_OF_VOTERS,
				Color.rgb(7, 98, 188),
				Color.WHITE,
				Optional.empty());

		addNomination(election, "Jens", "Poppinga", Gender.MALE, partyCdu);
		addNomination(election, "Dirk", "Eggers", Gender.MALE, partyCdu);
		addNomination(election, "Karsten", "Beck", Gender.MALE, partyCdu);
		addNomination(election, "Dietrich", "Motzkus", Gender.MALE, partyCdu);
		addNomination(election, "Sönke", "Behnk", Gender.MALE, partyCdu);
		addNomination(election, "Marcel", "Weger", Gender.MALE, partyCdu);
		addNomination(election, "Carola", "Gräpel", Gender.FEMALE, partyCdu);
		addNomination(election, "Rupert", "Schwarz", Gender.MALE, partyCdu);
		addNomination(election, "Erik", "Klein", Gender.MALE, partyCdu);
		addNomination(election, "Martina", "Dohrendorf", Gender.FEMALE, partyCdu);
		addNomination(election, "Christian", "Bernhardt", Gender.MALE, partyCdu);
		addNomination(election, "Andreas", "Topel", Gender.MALE, partyCdu);
		addNomination(election, "Dirk", "Kröger", Gender.MALE, partySpd);
		addNomination(election, "Ernst", "Eick", Gender.MALE, partySpd);
		addNomination(election, "Helga", "Jögimar", Gender.FEMALE, partySpd);
		addNomination(election, "Armin", "Ehlert", Gender.MALE, partySpd);
		addNomination(election, "Angelika", "Ziebarth", Gender.FEMALE, partySpd);
		addNomination(election, "Joachim", "Sauer", Gender.MALE, partySpd);
		addNomination(election, "Jan-Hendrik", "Gäde", Gender.MALE, partyAwg);
		addNomination(election, "Johannes", "Böttger", Gender.MALE, partyAwg);
		addNomination(election, "Volker", "Böttger", Gender.MALE, partyAwg);
		addNomination(election, "Martin", "Winter", Gender.MALE, partyAwg);
		addNomination(election, "Henning", "Gäde", Gender.MALE, partyAwg);
		addNomination(election, "Albert", "Stapelfeldt", Gender.MALE, partyAwg);
		addNomination(election, "Steffen", "Kühn", Gender.MALE, partyFwr);
		addNomination(election, "Joachim", "Wahl", Gender.MALE, partyFwr);
		addNomination(election, "Michael", "Kraus", Gender.MALE, partyFwr);
		addNomination(election, "Rolf", "Breede", Gender.MALE, partyFwr);
		addNomination(election, "Mathias", "Schöning", Gender.MALE, partyFwr);
		addNomination(election, "Eva-Maria", "König", Gender.FEMALE, partyFwr);
		addNomination(election, "Catrin", "Hartz", Gender.FEMALE, partyFwr);
		addNomination(election, "Thomas", "Dohrendorf", Gender.MALE, partyFwr);
		addNomination(election, "Joachim", "Efrom", Gender.MALE, partyFwr);
		addNomination(election, "Axel", "Feddern", Gender.MALE, partyFwr);
		addNomination(election, "Hartmut", "Feddern", Gender.MALE, partyFwr);
	}

	/**
	 * Adds a nomination for testing to {@code election}.
	 *
	 * @param election  Wahl
	 * @param firstName the given name
	 * @param lastName  the family name
	 * @param gender    the gender
	 * @param party     the party
	 */
	private static void addNomination(final LocalElection election,
			final String givenName,
			final String familyName,
			final Gender gender,
			final Party party) {
		election.createNomination(election.getDistrict().getChildren().iterator().next(),
				new Person(givenName,
						familyName,
						Optional.of(gender),
						OptionalInt.empty(),
						Optional.of(Locale.GERMAN),
						Optional.empty(),
						Optional.empty()),
				Optional.of(party));
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
				.orElseThrow(() -> new ElectionException("Cannot find a nomination named \"%s %s\".",
						givenName,
						familyName));
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
	@PackagePrivate
	static LocalPollingStation findPollingStation(final LocalElection election, final String name) {
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
