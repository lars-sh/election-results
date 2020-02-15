package de.larssh.election.germany.schleswigholstein.local;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectWriter;

import de.larssh.election.germany.schleswigholstein.Gender;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyType;
import de.larssh.election.germany.schleswigholstein.Person;
import javafx.scene.paint.Color;
import lombok.NoArgsConstructor;

/**
 * {@link LocalElection}
 */
@NoArgsConstructor
public class LocalElectionTest {
	public static LocalElection createElection() {
		final LocalDistrictRoot districtRoot
				= new LocalDistrictRoot("Gemeinde Rethwisch", LocalDistrictType.KREISANGEHOERIGE_GEMEINDE);

		final LocalElection election = new LocalElection(districtRoot, LocalDate.of(2018, 5, 6), "Gemeindewahl", 2);
		election.setPopulation(districtRoot, OptionalInt.empty());
		election.setNumberOfEligibleVoters(districtRoot, OptionalInt.empty());

		addDistricts(election);
		addPersons(election);

		return election;
	}

	private static void addDistricts(final LocalElection election) {
		final LocalDistrict districtLocal = election.getDistrict().createChild("Rethwisch");
		election.setPopulation(districtLocal, 1186);
		election.setNumberOfEligibleVoters(districtLocal, OptionalInt.empty());

		final LocalPollingStation pollingStationRethwischdorf = districtLocal.createChild("Rethwischdorf");
		election.setPopulation(pollingStationRethwischdorf, OptionalInt.empty());
		election.setNumberOfEligibleVoters(pollingStationRethwischdorf, 720);

		final LocalPollingStation pollingStationKleinBoden = districtLocal.createChild("Klein Boden");
		election.setPopulation(pollingStationKleinBoden, OptionalInt.empty());
		election.setNumberOfEligibleVoters(pollingStationKleinBoden, 270);
	}

	private static void addPersons(final LocalElection election) {
		final LocalDistrict district = election.getDistrict().getChildren().iterator().next();

		final Party partyCdu = new Party("CDU",
				"Christlich Demokratische Union Deutschlands, Ortsverband Rethwisch",
				PartyType.POLITICAL_PARTY,
				Color.BLACK,
				Color.WHITE);
		final Party partySpd = new Party("SPD",
				"Sozialdemokratische Partei Deutschlands, Ortsverband Rethwisch",
				PartyType.POLITICAL_PARTY,
				Color.rgb(237, 28, 36),
				Color.WHITE);
		final Party partyAwg = new Party("AWG",
				"Allgemeine Wählergemeinschaft Rethwisch",
				PartyType.ASSOCIATION_OF_VOTERS,
				Color.rgb(255, 204, 0),
				Color.BLACK);
		final Party partyFwr = new Party("FWR",
				"Freie Wählergemeinschaft Rethwisch",
				PartyType.ASSOCIATION_OF_VOTERS,
				Color.rgb(7, 98, 188),
				Color.WHITE);

		final Person personJensPoppinga = new Person("Jens",
				"Poppinga",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personJensPoppinga, Optional.of(partyCdu));
		final Person personDirkEggers = new Person("Dirk",
				"Eggers",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personDirkEggers, Optional.of(partyCdu));
		final Person personKarstenBeck = new Person("Karsten",
				"Beck",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personKarstenBeck, Optional.of(partyCdu));
		final Person personDietrichMotzkus = new Person("Dietrich",
				"Motzkus",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personDietrichMotzkus, Optional.of(partyCdu));
		final Person personSoenkeBehnk = new Person("Sönke",
				"Behnk",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personSoenkeBehnk, Optional.of(partyCdu));
		final Person personMarcelWeger = new Person("Marcel",
				"Weger",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personMarcelWeger, Optional.of(partyCdu));
		final Person personCarolaGraepel = new Person("Carola",
				"Gräpel",
				Optional.of(Gender.FEMALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personCarolaGraepel, Optional.of(partyCdu));
		final Person personRupertSchwarz = new Person("Rupert",
				"Schwarz",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personRupertSchwarz, Optional.of(partyCdu));
		final Person personErikKlein = new Person("Erik",
				"Klein",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personErikKlein, Optional.of(partyCdu));
		final Person personMartinaDohrendorf = new Person("Martina",
				"Dohrendorf",
				Optional.of(Gender.FEMALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personMartinaDohrendorf, Optional.of(partyCdu));
		final Person personChristianBernhardt = new Person("Christian",
				"Bernhardt",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personChristianBernhardt, Optional.of(partyCdu));
		final Person personAndreasTopel = new Person("Andreas",
				"Topel",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personAndreasTopel, Optional.of(partyCdu));
		final Person personDirkKroeger = new Person("Dirk",
				"Kröger",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personDirkKroeger, Optional.of(partySpd));
		final Person personErnstEick = new Person("Ernst",
				"Eick",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personErnstEick, Optional.of(partySpd));
		final Person personHelgaJoegimar = new Person("Helga",
				"Jögimar",
				Optional.of(Gender.FEMALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personHelgaJoegimar, Optional.of(partySpd));
		final Person personArminEhlert = new Person("Armin",
				"Ehlert",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personArminEhlert, Optional.of(partySpd));
		final Person personAngelikaZiebarth = new Person("Angelika",
				"Ziebarth",
				Optional.of(Gender.FEMALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personAngelikaZiebarth, Optional.of(partySpd));
		final Person personJoachimSauer = new Person("Joachim",
				"Sauer",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personJoachimSauer, Optional.of(partySpd));
		final Person personJanHendrikGaede = new Person("Jan-Hendrik",
				"Gäde",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personJanHendrikGaede, Optional.of(partyAwg));
		final Person personJohannesBoettger = new Person("Johannes",
				"Böttger",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personJohannesBoettger, Optional.of(partyAwg));
		final Person personVolkerBoettger = new Person("Volker",
				"Böttger",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personVolkerBoettger, Optional.of(partyAwg));
		final Person personMartinWinter = new Person("Martin",
				"Winter",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personMartinWinter, Optional.of(partyAwg));
		final Person personHenningGaede = new Person("Henning",
				"Gäde",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personHenningGaede, Optional.of(partyAwg));
		final Person personAlbertStapelfeldt = new Person("Albert",
				"Stapelfeldt",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personAlbertStapelfeldt, Optional.of(partyAwg));
		final Person personSteffenKuehn = new Person("Steffen",
				"Kühn",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personSteffenKuehn, Optional.of(partyFwr));
		final Person personJoachimWahl = new Person("Joachim",
				"Wahl",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personJoachimWahl, Optional.of(partyFwr));
		final Person personMichaelKraus = new Person("Michael",
				"Kraus",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personMichaelKraus, Optional.of(partyFwr));
		final Person personRolfBreede = new Person("Rolf",
				"Breede",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personRolfBreede, Optional.of(partyFwr));
		final Person personMathiasSchoening = new Person("Mathias",
				"Schöning",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personMathiasSchoening, Optional.of(partyFwr));
		final Person personEvaMariaKoenig = new Person("Eva-Maria",
				"König",
				Optional.of(Gender.FEMALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personEvaMariaKoenig, Optional.of(partyFwr));
		final Person personCatrinHartz = new Person("Catrin",
				"Hartz",
				Optional.of(Gender.FEMALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personCatrinHartz, Optional.of(partyFwr));
		final Person personThomasDohrendorf = new Person("Thomas",
				"Dohrendorf",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personThomasDohrendorf, Optional.of(partyFwr));
		final Person personJoachimEfrom = new Person("Joachim",
				"Efrom",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personJoachimEfrom, Optional.of(partyFwr));
		final Person personAxelFeddern = new Person("Axel",
				"Feddern",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personAxelFeddern, Optional.of(partyFwr));
		final Person personHartmutFeddern = new Person("Hartmut",
				"Feddern",
				Optional.of(Gender.MALE),
				OptionalInt.empty(),
				Optional.of(Locale.GERMAN),
				Optional.empty(),
				Optional.empty());
		election.createNomination(district, personHartmutFeddern, Optional.of(partyFwr));
	}

	@Test
	public void testJacksonForElection() throws IOException {
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
		assertEquals(electionCreatedToString, electionOpenedToString);
	}
}
