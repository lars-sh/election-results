package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashSet;
import static de.larssh.utils.Collectors.toMap;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.DistrictValueMap;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;
import de.larssh.utils.Nullables;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wahl der Vertretung einer Gemeinde oder eines Kreises in Schleswig-Holstein
 * gem. Gemeinde- und Kreiswahlgesetz (GKWG)
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.GodClass" })
public class LocalElection implements Election<LocalDistrictRoot, LocalNomination> {
	/**
	 * A JSON {@link ObjectMapper} compatible with {@link LocalElection}
	 */
	@PackagePrivate
	static final ObjectMapper OBJECT_MAPPER = new ObjectMapper() //
			.registerModule(new JavaTimeModule())
			.registerModule(new Jdk8Module())
			.registerModule(new ParameterNamesModule());

	/**
	 * Creates a new JSON {@link ObjectWriter} compatible with
	 * {@link LocalElection}.
	 *
	 * @return the created JSON {@link ObjectWriter}
	 */
	public static ObjectWriter createJacksonObjectWriter() {
		return OBJECT_MAPPER.writer();
	}

	/**
	 * Creates a new {@link LocalElection} based on provided JSON data.
	 *
	 * @param reader the JSON data
	 * @return the created {@link LocalElection}
	 * @throws IOException on IO error
	 */
	public static LocalElection fromJson(final Reader reader) throws IOException {
		return OBJECT_MAPPER.readValue(reader, LocalElection.class);
	}

	/**
	 * Name of the election
	 *
	 * @return the name of the election
	 */
	@JsonProperty(access = Access.READ_ONLY, index = 0)
	@EqualsAndHashCode.Include
	String name;

	/**
	 * Date of the election
	 *
	 * @return the date of the election
	 */
	@JsonProperty(access = Access.READ_ONLY, index = 1)
	@EqualsAndHashCode.Include
	LocalDate date;

	/**
	 * Wahlgebiet
	 */
	@JsonProperty(access = Access.READ_ONLY, index = 2)
	@EqualsAndHashCode.Include
	LocalDistrictRoot district;

	/**
	 * Einwohnerzahl nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 */
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	@JsonProperty(access = Access.READ_ONLY, index = 3)
	DistrictValueMap population = new DistrictValueMap(this);

	/**
	 * Anzahl der Wahlberechtigten nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 */
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	@JsonProperty(access = Access.READ_ONLY, index = 4)
	DistrictValueMap numberOfEligibleVoters = new DistrictValueMap(this);

	/**
	 * Bewerberinnen und Bewerber
	 */
	@ToString.Exclude
	Set<LocalNomination> nominations = new LinkedHashSet<>();

	/**
	 * Wahlgebiet, Wahlkreise und Wahlbezirke
	 */
	@ToString.Exclude
	Supplier<Set<District<?>>> allDistricts = lazy(() -> getDistrict().getAllChildren());

	/**
	 * Bewerberinnen und Bewerber
	 *
	 * <p>
	 * This field stores an unmodifiable copy of the nominations as list for
	 * internal purpose only, as some comparators need it.
	 */
	@ToString.Exclude
	List<LocalNomination> nominationsAsList = new ArrayList<>();

	/**
	 * Wahl
	 *
	 * @param parsable JSON delegate
	 */
	@JsonCreator(mode = Mode.DELEGATING)
	@SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR",
			justification = "passing this to create*For, but made sure, it's filling the object in the correct order")
	private LocalElection(final ParsableLocalElection parsable) {
		this(parsable.getName(), parsable.getDate(), parsable.getDistrict());

		population.putAllByKey(parsable.getPopulation());
		numberOfEligibleVoters.putAllByKey(parsable.getNumberOfEligibleVoters());
		parsable.addNominationsTo(this);
	}

	/**
	 * Wahlgebiet, Wahlkreise und Wahlbezirke
	 *
	 * @return Wahlgebiet, Wahlkreise und Wahlbezirke
	 */
	@JsonIgnore
	public Set<District<?>> getAllDistricts() {
		return allDistricts.get();
	}

	/**
	 * Collects all {@link LocalPollingStation}s of this election.
	 *
	 * @return all {@link LocalPollingStation}s
	 */
	@JsonIgnore
	public Set<LocalPollingStation> getPollingStations() {
		return getDistrict().getChildren()
				.stream()
				.map(LocalDistrict::getChildren)
				.flatMap(Set::stream)
				.collect(toLinkedHashSet());
	}

	/** {@inheritDoc} */
	@Override
	public OptionalInt getPopulation(final District<?> district) {
		return population.get(district);
	}

	/** {@inheritDoc} */
	@Override
	public void setPopulation(final District<?> district, final OptionalInt population) {
		this.population.put(district, population);
	}

	/** {@inheritDoc} */
	@Override
	public OptionalInt getNumberOfEligibleVoters(final District<?> district) {
		return numberOfEligibleVoters.get(district);
	}

	/** {@inheritDoc} */
	@Override
	public void setNumberOfEligibleVoters(final District<?> district, final OptionalInt numberOfEligibleVoters) {
		this.numberOfEligibleVoters.put(district, numberOfEligibleVoters);
	}

	/**
	 * Creates and registers a new nomination.
	 *
	 * @param district Wahlkreis
	 * @param person   Bewerberin oder Bewerber
	 * @param party    Gruppierung
	 * @return the new nomination
	 */
	public LocalNomination createNomination(final LocalDistrict district,
			final Person person,
			final Optional<Party> party) {
		final LocalNomination nomination = new LocalNomination(this, district, party, person);
		if (nominations.contains(nomination)) {
			throw new ElectionException("Nomination \"%s\" for district \"%s\" cannot be added twice.",
					nomination.getKey(),
					district.getKey());
		}
		nominations.add(nomination);
		return nomination;
	}

	/** {@inheritDoc} */
	@Override
	public Set<LocalNomination> getNominations() {
		return unmodifiableSet(nominations);
	}

	/**
	 * Bewerberinnen und Bewerber nach Gruppierung
	 *
	 * @param party Gruppierung
	 * @return the nominations
	 */
	public Set<LocalNomination> getNominations(final Party party) {
		return getNominations().stream()
				.filter(nomination -> nomination.getParty().filter(party::equals).isPresent())
				.collect(toLinkedHashSet());
	}

	/**
	 * Bewerberinnen und Bewerber
	 *
	 * <p>
	 * This method returns an unmodifiable copy of the nominations as list for
	 * internal purpose only, as some comparators need it.
	 *
	 * @return Bewerberinnen und Bewerber
	 */
	@PackagePrivate
	List<LocalNomination> getNominationsAsList() {
		final Set<LocalNomination> nominations = getNominations();
		if (nominationsAsList.size() != nominations.size()) {
			nominationsAsList.clear();
			nominationsAsList.addAll(nominations);
		}
		return unmodifiableList(nominationsAsList);
	}

	/**
	 * Anzahl der unmittelbaren Vertreterinnen und Vertreter im Wahlgebiet (§ 8 Kom
	 * WG SH)
	 *
	 * @return Anzahl der unmittelbaren Vertreterinnen und Vertreter im Wahlgebiet
	 */
	@JsonIgnore
	public int getNumberOfDirectSeats() {
		return PopulationInformation.get(getDistrict().getType()).getNumberOfDirectSeats(getPopulation());
	}

	/**
	 * Anzahl der unmittelbaren Vertreterinnen und Vertreter pro Wahlkreis (§ 9
	 * Absätze 1-3 GKWG)
	 *
	 * @return Anzahl der unmittelbaren Vertreterinnen und Vertreter pro Wahlkreis
	 */
	@JsonIgnore
	public int getNumberOfDirectSeatsPerLocalDistrict() {
		return getNumberOfDirectSeats() / getNumberOfDistricts();
	}

	/**
	 * Anzahl der Wahlkreise (§ 9 Absätze 1-3 GKWG)
	 *
	 * @return Anzahl der Wahlkreise
	 */
	@JsonIgnore
	public int getNumberOfDistricts() {
		return PopulationInformation.get(getDistrict().getType()).getNumberOfDistricts(getPopulation());
	}

	/**
	 * Anzahl der Listenvertreterinnen und Listenvertreter (§ 8 GKWG)
	 *
	 * @return Anzahl der Listenvertreterinnen und Listenvertreter
	 */
	@JsonIgnore
	public int getNumberOfListSeats() {
		return getNumberOfDirectSeats() - 1;
	}

	/**
	 * Gesamtzahl der Vertreterinnen und Vertreter (§ 8 GKWG)
	 *
	 * @return Gesamtzahl der Vertreterinnen und Vertreter
	 */
	@JsonIgnore
	public int getNumberOfSeats() {
		return getNumberOfDirectSeats() + getNumberOfListSeats();
	}

	/**
	 * Maximale Anzahl Stimmen pro Stimmzettel (§ 9 Absatz 4 GKWG)
	 *
	 * @return Maximale Anzahl Stimmen pro Stimmzettel
	 */
	@JsonIgnore
	public int getNumberOfVotesPerBallot() {
		return getNumberOfDirectSeatsPerLocalDistrict();
	}

	/**
	 * Politische Parteien und Wählergruppen
	 *
	 * @return Politische Parteien und Wählergruppen
	 */
	@JsonProperty(index = 6)
	@SuppressWarnings("checkstyle:MagicNumber")
	public Set<Party> getParties() {
		return getNominations().stream()
				.map(LocalNomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toCollection(TreeSet::new));
	}

	/**
	 * JSON delegate for {@link LocalElection}
	 */
	@Getter
	@SuppressWarnings("PMD.DataClass")
	private static class ParsableLocalElection {
		/**
		 * Wahlgebiet
		 */
		LocalDistrictRoot district;

		/**
		 * Date of the election
		 *
		 * @return the date of the election
		 */
		LocalDate date;

		/**
		 * Name of the election
		 *
		 * @return the name of the election
		 */
		String name;

		/**
		 * Einwohnerzahl nach Wahlgebiet, Wahlkreis oder Wahlbezirk
		 *
		 * @return Einwohnerzahl nach Wahlgebiet, Wahlkreis oder Wahlbezirk
		 */
		Map<String, OptionalInt> population;

		/**
		 * Anzahl der Wahlberechtigten nach Wahlgebiet, Wahlkreis oder Wahlbezirk
		 *
		 * @return Anzahl der Wahlberechtigten nach Wahlgebiet, Wahlkreis oder
		 *         Wahlbezirk
		 */
		Map<String, OptionalInt> numberOfEligibleVoters;

		/**
		 * Bewerberinnen und Bewerber
		 *
		 * <p>
		 * Elements of this list are guaranteed to be distinct.
		 */
		List<ParsableLocalNomination> nominations;

		/**
		 * Politische Parteien und Wählergruppen
		 *
		 * @return Politische Parteien und Wählergruppen
		 */
		Set<Party> parties;

		/**
		 * JSON delegate for {@link LocalElection}
		 *
		 * @param district               Wahlgebiet
		 * @param date                   Date
		 * @param name                   Name
		 * @param population             Einwohnerzahl nach Wahlgebiet, Wahlkreis oder
		 *                               Wahlbezirk
		 * @param numberOfEligibleVoters Anzahl der Wahlberechtigten nach Wahlgebiet,
		 *                               Wahlkreis oder Wahlbezirk
		 * @param nominations            Bewerberinnen und Bewerber
		 * @param parties                Politische Parteien und Wählergruppen
		 */
		@SuppressWarnings("checkstyle:ParameterNumber")
		private ParsableLocalElection(@Nullable final LocalDistrictRoot district,
				@Nullable final LocalDate date,
				@Nullable final String name,
				@Nullable final Map<String, OptionalInt> population,
				@Nullable final Map<String, OptionalInt> numberOfEligibleVoters,
				@Nullable final List<ParsableLocalNomination> nominations,
				@Nullable final Set<Party> parties) {
			this.district = Nullables.orElseThrow(district,
					() -> new ElectionException("Missing required parameter \"district\" for election."));
			this.date = Nullables.orElseThrow(date,
					() -> new ElectionException("Missing required parameter \"date\" for election."));
			this.name = Nullables.orElseThrow(name,
					() -> new ElectionException("Missing required parameter \"type\" for election."));
			this.population = Nullables.orElseGet(population, Collections::emptyMap);
			this.numberOfEligibleVoters = Nullables.orElseGet(numberOfEligibleVoters, Collections::emptyMap);
			this.nominations = Nullables.orElseGet(nominations, Collections::emptyList);
			this.parties = Nullables.orElseGet(parties, Collections::emptySet);
		}

		/**
		 * Adds nominations to {@code election}.
		 *
		 * @param election Wahl
		 */
		public void addNominationsTo(final LocalElection election) {
			final Map<String, District<?>> districts
					= election.getAllDistricts().stream().collect(toMap(District::getKey, identity()));
			final Map<String, Party> parties = getParties().stream().collect(toMap(Party::getKey, identity()));

			for (final ParsableLocalNomination nomination : getNominations()) {
				final District<?> district = districts.get(nomination.getDistrict());
				if (district == null) {
					throw new ElectionException("District \"%s\" of nomination \"%s, %s\" does not exist.",
							nomination.getDistrict(),
							nomination.getPerson().getFamilyName(),
							nomination.getPerson().getGivenName());
				}
				if (!(district instanceof LocalDistrict)) {
					throw new ElectionException(
							"District \"%s\" of nomination \"%s, %s\" is of type %s. Expecting type %s for nominations.",
							nomination.getDistrict(),
							nomination.getPerson().getFamilyName(),
							nomination.getPerson().getGivenName(),
							district.getClass().getSimpleName(),
							LocalDistrict.class.getSimpleName());
				}

				final Optional<Party> party = nomination.getParty().map(parties::get);
				if (nomination.getParty().isPresent() && !party.isPresent()) {
					throw new ElectionException("Party \"%s\" of nomination \"%s, %s\" does not exist.",
							nomination.getParty().get(),
							nomination.getPerson().getFamilyName(),
							nomination.getPerson().getGivenName());
				}

				election.createNomination((LocalDistrict) district, nomination.getPerson(), party);
			}
		}
	}

	/**
	 * JSON delegate for {@link LocalNomination}
	 */
	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalNomination {
		/**
		 * Wahlkreis
		 *
		 * @return Wahlkreis
		 */
		String district;

		/**
		 * Politische Partei, Wählergruppe oder empty für unabhängige Bewerberinnen und
		 * Bewerber
		 *
		 * @return Politische Partei, Wählergruppe oder empty
		 */
		Optional<String> party;

		/**
		 * Bewerberin oder Bewerber
		 *
		 * @return Bewerberin oder Bewerber
		 */
		Person person;
	}
}
