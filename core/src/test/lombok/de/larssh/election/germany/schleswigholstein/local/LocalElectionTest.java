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

		final LocalPollingStation pollingStationKleinBoden
				= districtLocal.createChild(POLLING_STATION_NAME_KLEIN_BODEN);
		pollingStationKleinBoden.setBackgroundColor(Color.rgb(7, 98, 188));
		election.setPopulation(pollingStationKleinBoden, OptionalInt.empty());
		election.setNumberOfEligibleVoters(pollingStationKleinBoden, 273);

		final LocalPollingStation pollingStationRethwischdorf
				= districtLocal.createChild(POLLING_STATION_NAME_RETHWISCHDORF);
		pollingStationRethwischdorf.setBackgroundColor(Color.rgb(237, 28, 36));
		election.setPopulation(pollingStationRethwischdorf, OptionalInt.empty());
		election.setNumberOfEligibleVoters(pollingStationRethwischdorf, 717);
	}

	/**
	 * Adds parties and nominations for testing to {@code election}.
	 *
	 * @param election Wahl
	 */
	private static void addPartiesAndNominations(final LocalElection election) {
		final Party partyCdu = new Party("CDU", //
				"Christlich Demokratische Union Deutschlands, Ortsverband Rethwisch",
				PartyType.POLITICAL_PARTY);
		partyCdu.setBackgroundColor(Color.rgb(51, 51, 51));
		final Party partySpd = new Party("SPD", //
				"Sozialdemokratische Partei Deutschlands, Ortsverband Rethwisch",
				PartyType.POLITICAL_PARTY);
		partySpd.setBackgroundColor(Color.rgb(237, 28, 36));
		final Party partyAwg = new Party("AWG", //
				"Allgemeine Wählergemeinschaft Rethwisch",
				PartyType.ASSOCIATION_OF_VOTERS);
		partyAwg.setBackgroundColor(Color.rgb(255, 204, 0));
		partyAwg.setFontColor(Color.BLACK);
		final Party partyFwr = new Party("FWR", //
				"Freie Wählergemeinschaft Rethwisch",
				PartyType.ASSOCIATION_OF_VOTERS);
		partyFwr.setBackgroundColor(Color.rgb(7, 98, 188));

		addNomination(election, partyCdu, "Poppinga", "Jens", Gender.MALE);
		addNomination(election, partyCdu, "Eggers", "Dirk", Gender.MALE);
		addNomination(election, partyCdu, "Beck", "Karsten", Gender.MALE);
		addNomination(election, partyCdu, "Motzkus", "Dietrich", Gender.MALE);
		addNomination(election, partyCdu, "Behnk", "Sönke", Gender.MALE);
		addNomination(election, partyCdu, "Weger", "Marcel", Gender.MALE);
		addNomination(election, partyCdu, "Gräpel", "Carola", Gender.FEMALE);
		addNomination(election, partyCdu, "Schwarz", "Rupert", Gender.MALE);
		addNomination(election, partyCdu, "Klein", "Erik", Gender.MALE);
		addNomination(election, partyCdu, "Dohrendorf", "Martina", Gender.FEMALE);
		addNomination(election, partyCdu, "Bernhardt", "Christian", Gender.MALE);
		addNomination(election, partyCdu, "Topel", "Andreas", Gender.MALE);
		addNomination(election, partySpd, "Kröger", "Dirk", Gender.MALE);
		addNomination(election, partySpd, "Eick", "Ernst", Gender.MALE);
		addNomination(election, partySpd, "Jögimar", "Helga", Gender.FEMALE);
		addNomination(election, partySpd, "Ehlert", "Armin", Gender.MALE);
		addNomination(election, partySpd, "Ziebarth", "Angelika", Gender.FEMALE);
		addNomination(election, partySpd, "Sauer", "Joachim", Gender.MALE);
		addNomination(election, partyAwg, "Gäde", "Jan-Hendrik", Gender.MALE);
		addNomination(election, partyAwg, "Böttger", "Johannes", Gender.MALE);
		addNomination(election, partyAwg, "Böttger", "Volker", Gender.MALE);
		addNomination(election, partyAwg, "Winter", "Martin", Gender.MALE);
		addNomination(election, partyAwg, "Gäde", "Henning", Gender.MALE);
		addNomination(election, partyAwg, "Stapelfeldt", "Albert", Gender.MALE);
		addNomination(election, partyFwr, "Kühn", "Steffen", Gender.MALE);
		addNomination(election, partyFwr, "Wahl", "Joachim", Gender.MALE);
		addNomination(election, partyFwr, "Kraus", "Michael", Gender.MALE);
		addNomination(election, partyFwr, "Breede", "Rolf", Gender.MALE);
		addNomination(election, partyFwr, "Schöning", "Mathias", Gender.MALE);
		addNomination(election, partyFwr, "König", "Eva-Maria", Gender.FEMALE);
		addNomination(election, partyFwr, "Hartz", "Catrin", Gender.FEMALE);
		addNomination(election, partyFwr, "Dohrendorf", "Thomas", Gender.MALE);
		addNomination(election, partyFwr, "Efrom", "Joachim", Gender.MALE);
		addNomination(election, partyFwr, "Feddern", "Axel", Gender.MALE);
		addNomination(election, partyFwr, "Feddern", "Hartmut", Gender.MALE);
	}

	/**
	 * Adds a nomination for testing to {@code election}.
	 *
	 * @param election   Wahl
	 * @param party      the party
	 * @param familyName the family name
	 * @param givenName  the given name
	 * @param gender     the gender
	 */
	private static void addNomination(final LocalElection election,
			final Party party,
			final String familyName,
			final String givenName,
			final Gender gender) {
		election.createNomination(election.getDistrict().getChildren().iterator().next(),
				new Person(familyName,
						givenName,
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
