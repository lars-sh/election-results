package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashSet;
import static de.larssh.utils.Collectors.toMap;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import de.larssh.election.germany.schleswigholstein.District;
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
	 * Wahlgebiet
	 */
	@EqualsAndHashCode.Include
	LocalDistrictRoot district;

	/**
	 * Date of the election
	 *
	 * @return the date of the election
	 */
	@EqualsAndHashCode.Include
	LocalDate date;

	/**
	 * Name of the election
	 *
	 * @return the name of the election
	 */
	@EqualsAndHashCode.Include
	String name;

	/**
	 * Einwohnerzahl nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 */
	@JsonIgnore
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> population = new TreeMap<>();

	/**
	 * Anzahl der Wahlberechtigten nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 */
	@JsonIgnore
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> numberOfEligibleVoters = new TreeMap<>();

	/**
	 * Bewerberinnen und Bewerber
	 */
	@ToString.Exclude
	Set<LocalNomination> nominations = new LinkedHashSet<>();

	/**
	 * Scale (decimal places) of Sainte Laguë values
	 *
	 * <p>
	 * Usually this is {@code 2}.
	 *
	 * @return the scale (decimal places) of Sainte Laguë values
	 */
	int sainteLagueScale;

	/**
	 * Wahlgebiet, Wahlkreise und Wahlbezirke
	 */
	@ToString.Exclude
	Supplier<Set<District<?>>> districts = lazy(() -> {
		final Set<District<?>> districts = new HashSet<>();

		// Root
		districts.add(getDistrict());

		// Districts
		districts.addAll(getDistrict().getChildren());

		// Polling Stations
		for (final LocalDistrict district : getDistrict().getChildren()) {
			districts.addAll(district.getChildren());
		}

		return unmodifiableSet(districts);
	});

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
		this(parsable.getDistrict(), parsable.getDate(), parsable.getName(), parsable.getSainteLagueScale());

		parsable.addPopulationTo(this);
		parsable.addNumberOfEligibleVotersTo(this);
		parsable.addNominationsTo(this);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressFBWarnings(value = "OI_OPTIONAL_ISSUES_CHECKING_REFERENCE", justification = "optimized map contains")
	public OptionalInt getPopulation(final District<?> district) {
		final OptionalInt population = this.population.get(district);
		if (population != null && population.isPresent()) {
			return population;
		}

		final Set<? extends District<?>> children = district.getChildren();
		if (children.isEmpty()) {
			return OptionalInt.empty();
		}

		int calculated = 0;
		for (final District<?> child : children) {
			final OptionalInt populationOfChild = getPopulation(child);
			if (!populationOfChild.isPresent()) {
				return OptionalInt.empty();
			}
			calculated += populationOfChild.getAsInt();
		}
		return OptionalInt.of(calculated);
	}

	/**
	 * Population information as JSON property
	 *
	 * @return Einwohnerzahl nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 */
	@JsonProperty("population")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "JSON property")
	private Map<String, OptionalInt> getPopulationForJackson() {
		return population.entrySet()
				.stream()
				.collect(toMap(entry -> entry.getKey().getKey(), Entry::getValue, TreeMap::new));
	}

	/** {@inheritDoc} */
	@Override
	public void setPopulation(final District<?> district, final OptionalInt population) {
		setDistrictsMap(this.population, "population", district, population);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressFBWarnings(value = "OI_OPTIONAL_ISSUES_CHECKING_REFERENCE", justification = "optimized map contains")
	public OptionalInt getNumberOfEligibleVoters(final District<?> district) {
		final OptionalInt numberOfEligibleVoters = this.numberOfEligibleVoters.get(district);
		if (numberOfEligibleVoters != null && numberOfEligibleVoters.isPresent()) {
			return numberOfEligibleVoters;
		}

		final Set<? extends District<?>> children = district.getChildren();
		if (children.isEmpty()) {
			return OptionalInt.empty();
		}

		int calculated = 0;
		for (final District<?> child : children) {
			final OptionalInt numberOfEligibleVotersOfChild = getNumberOfEligibleVoters(child);
			if (!numberOfEligibleVotersOfChild.isPresent()) {
				return OptionalInt.empty();
			}
			calculated += numberOfEligibleVotersOfChild.getAsInt();
		}
		return OptionalInt.of(calculated);
	}

	/**
	 * The number of eligible voters as JSON property
	 *
	 * @return Anzahl der Wahlberechtigten nach Wahlgebiet, Wahlkreis oder
	 *         Wahlbezirk
	 */
	@JsonProperty("numberOfEligibleVoters")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "JSON property")
	private Map<String, OptionalInt> getNumberOfEligibleVotersForJackson() {
		return numberOfEligibleVoters.entrySet()
				.stream()
				.collect(toMap(entry -> entry.getKey().getKey(), Entry::getValue, TreeMap::new));
	}

	/** {@inheritDoc} */
	@Override
	public void setNumberOfEligibleVoters(final District<?> district, final OptionalInt numberOfEligibleVoters) {
		setDistrictsMap(this.numberOfEligibleVoters, "number of eligible voters", district, numberOfEligibleVoters);
	}

	/**
	 * Creates and registers a new nomination.
	 *
	 * @param district Wahlkreis
	 * @param person   Berwerberin oder Bewerber
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
	 * Berwerberinnen und Bewerber nach Gruppierung
	 *
	 * @param party Gruppierung
	 * @return the nominations
	 */
	public Set<LocalNomination> getNominationsOfParty(final Party party) {
		return getNominations().stream()
				.filter(nomination -> nomination.getParty().filter(party::equals).isPresent())
				.collect(toLinkedHashSet());
	}

	/**
	 * Wahlgebiet, Wahlkreise und Wahlbezirke
	 *
	 * @return Wahlgebiet, Wahlkreise und Wahlbezirke
	 */
	@JsonIgnore
	public Set<District<?>> getDistricts() {
		return districts.get();
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
	public Set<Party> getParties() {
		return getNominations().stream()
				.map(LocalNomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toCollection(TreeSet::new));
	}

	/**
	 * Updates {@code map} by putting {@code value} for the key {@code district}.
	 * There must not be such an entry yet and the {@code district} must be part of
	 * the root district.
	 *
	 * <p>
	 * {@code type} is used in error case only.
	 *
	 * @param <T>      the type of {@code value}
	 * @param map      the map to update
	 * @param type     the maps kind
	 * @param district the district to update
	 * @param value    the value to insert
	 */
	private <T> void setDistrictsMap(final Map<District<?>, T> map,
			final String type,
			final District<?> district,
			final T value) {
		if (!district.getRoot().equals(getDistrict())) {
			throw new ElectionException("District \"%s\" is not part of the elections district hierarchy.",
					district.getKey());
		}
		if (map.containsKey(district)) {
			throw new ElectionException("The %s has already been set for district \"%s\".", type, district.getKey());
		}
		map.put(district, value);
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
		 * Scale (decimal places) of Sainte Laguë values
		 *
		 * <p>
		 * Usually this is {@code 2}.
		 *
		 * @return the scale (decimal places) of Sainte Laguë values
		 */
		int sainteLagueScale;

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
		 * @param sainteLagueScale       Scale (decimal places) of Sainte Laguë values
		 * @param parties                Politische Parteien und Wählergruppen
		 */
		@SuppressWarnings("checkstyle:ParameterNumber")
		private ParsableLocalElection(@Nullable final LocalDistrictRoot district,
				@Nullable final LocalDate date,
				@Nullable final String name,
				@Nullable final Map<String, OptionalInt> population,
				@Nullable final Map<String, OptionalInt> numberOfEligibleVoters,
				@Nullable final List<de.larssh.election.germany.schleswigholstein.local.LocalElection.ParsableLocalNomination> nominations,
				@Nullable final Integer sainteLagueScale,
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
			this.sainteLagueScale = Nullables.orElse(sainteLagueScale, 2);
			this.parties = Nullables.orElseGet(parties, Collections::emptySet);
		}

		/**
		 * Adds population information to {@code election}.
		 *
		 * @param election Wahl
		 */
		public void addPopulationTo(final LocalElection election) {
			final Map<String, OptionalInt> population = getPopulation();

			for (final District<?> district : election.getDistricts()) {
				election.setPopulation(district, population.getOrDefault(district.getKey(), OptionalInt.empty()));
			}
		}

		/**
		 * Adds the numbers of eligible voters to {@code election}.
		 *
		 * @param election Wahl
		 */
		public void addNumberOfEligibleVotersTo(final LocalElection election) {
			final Map<String, OptionalInt> numberOfEligibleVoters = getNumberOfEligibleVoters();

			for (final District<?> district : election.getDistricts()) {
				election.setNumberOfEligibleVoters(district,
						numberOfEligibleVoters.getOrDefault(district.getKey(), OptionalInt.empty()));
			}
		}

		/**
		 * Adds nominations to {@code election}.
		 *
		 * @param election Wahl
		 */
		public void addNominationsTo(final LocalElection election) {
			final Map<String, District<?>> districts
					= election.getDistricts().stream().collect(toMap(District::getKey, identity()));
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
	@PackagePrivate
	@RequiredArgsConstructor
	static class ParsableLocalNomination {
		/**
		 * Creates a set of {@link LocalNomination} based on a given set of
		 * {@link ParsableLocalNomination}.
		 *
		 * @param election Wahl
		 * @param set      the set of {@link ParsableLocalNomination}
		 * @return the set of {@link LocalNomination}
		 */
		public static Set<LocalNomination> convert(final LocalElection election,
				final Set<ParsableLocalNomination> set) {
			final Map<String, LocalNomination> nominations
					= election.getNominations().stream().collect(toMap(LocalNomination::getKey, identity()));

			return set.stream()
					.map(nomination -> LocalNomination.createKey(nomination.getPerson().getKey(),
							nomination.getParty()))
					.map(key -> {
						final LocalNomination nomination = nominations.get(key);
						if (nomination == null) {
							throw new ElectionException(
									"Could not find nomination with key \"%s\" for election \"%s\".",
									key,
									election.getName());
						}
						return nomination;
					})
					.collect(toSet());
		}

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
