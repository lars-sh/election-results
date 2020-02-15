package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toMap;
import static de.larssh.utils.Finals.constant;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
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
import javafx.scene.paint.Color;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LocalElection implements Election {
	@PackagePrivate
	static final ObjectMapper OBJECT_MAPPER = new ObjectMapper() //
			.addMixIn(Color.class, ColorMixIn.class)
			.registerModule(new JavaTimeModule())
			.registerModule(new Jdk8Module())
			.registerModule(new ParameterNamesModule());

	public static final int SAINTE_LAGUE_SCALE_DEFAULT = constant(2);

	public static ObjectWriter createJacksonObjectWriter() {
		return OBJECT_MAPPER.writer();
	}

	public static LocalElection fromJson(final Reader reader) throws IOException {
		return OBJECT_MAPPER.readValue(reader, LocalElection.class);
	}

	LocalDistrictRoot district;

	@EqualsAndHashCode.Include
	LocalDate date;

	@EqualsAndHashCode.Include
	String name;

	@JsonIgnore
	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> population = new TreeMap<>();

	@JsonIgnore
	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> numberOfEligibleVoters = new TreeMap<>();

	List<LocalNomination> nominations = new ArrayList<>();

	int sainteLagueScale;

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

	@JsonCreator(mode = Mode.DELEGATING)
	private LocalElection(final ParsableLocalElection parsable) {
		this(parsable.getDistrict(), parsable.getDate(), parsable.getName(), parsable.getSainteLagueScale());

		parsable.createPopulationFor(this);
		parsable.createNumberOfEligibleVotersFor(this);
		parsable.createNominationsFor(this);
	}

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

	@JsonProperty("population")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "JSON property")
	private Map<String, OptionalInt> getPopulationForJackson() {
		return population.entrySet()
				.stream()
				.collect(toMap(entry -> entry.getKey().getKey(), Entry::getValue, TreeMap::new));
	}

	@Override
	public void setPopulation(final District<?> district, final OptionalInt population) {
		setDistrictsMap(this.population, "population", district, population);
	}

	@Override
	@SuppressFBWarnings(value = "OI_OPTIONAL_ISSUES_CHECKING_REFERENCE", justification = "optimized map contains")
	public OptionalInt getNumberOfEligibleVoters(final District<?> district) { // TODO: getOrSumByChildren
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

	@JsonProperty("numberOfEligibleVoters")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "JSON property")
	private Map<String, OptionalInt> getNumberOfEligibleVotersForJackson() {
		return numberOfEligibleVoters.entrySet()
				.stream()
				.collect(toMap(entry -> entry.getKey().getKey(), Entry::getValue, TreeMap::new));
	}

	@Override
	public void setNumberOfEligibleVoters(final District<?> district, final OptionalInt numberOfEligibleVoters) {
		setDistrictsMap(this.numberOfEligibleVoters, "number of eligible voters", district, numberOfEligibleVoters);
	}

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

	@Override
	public List<LocalNomination> getNominations() {
		return unmodifiableList(nominations);
	}

	public List<LocalNomination> getNominationsOfParty(final Party party) {
		return getNominations().stream()
				.filter(nomination -> nomination.getParty().isPresent() && nomination.getParty().get().equals(party))
				.collect(toList());
	}

	@JsonIgnore
	public Set<District<?>> getDistricts() {
		return districts.get();
	}

	@JsonIgnore
	public int getNumberOfDirectSeats() {
		return PopulationInformation.get(getDistrict().getType()).getNumberOfDirectSeats(getPopulation());
	}

	@JsonIgnore
	public int getNumberOfDirectSeatsPerLocalDistrict() {
		return getNumberOfDirectSeats() / getNumberOfDistricts();
	}

	@JsonIgnore
	public int getNumberOfDistricts() {
		return PopulationInformation.get(getDistrict().getType()).getNumberOfDistricts(getPopulation());
	}

	@JsonIgnore
	public int getNumberOfListSeats() {
		return getNumberOfDirectSeats() - 1;
	}

	@JsonIgnore
	public int getNumberOfSeats() {
		return getNumberOfDirectSeats() + getNumberOfListSeats();
	}

	@JsonIgnore
	public int getNumberOfVotesPerBallot() {
		return getNumberOfDirectSeatsPerLocalDistrict();
	}

	public Set<Party> getParties() {
		return getNominations().stream()
				.map(LocalNomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toCollection(TreeSet::new));
	}

	private void setDistrictsMap(final Map<District<?>, OptionalInt> map,
			final String type,
			final District<?> district,
			final OptionalInt value) {
		if (!district.getRoot().equals(getDistrict())) {
			throw new ElectionException("District \"%s\" is not part of the elections district hierarchy.",
					district.getKey());
		}
		if (map.containsKey(district)) {
			throw new ElectionException("The %s has already been set for district \"%s\".", type, district.getKey());
		}
		map.put(district, value);
	}

	@SuppressWarnings("PMD.CommentDefaultAccessModifier")
	private abstract static class ColorMixIn {
		@SuppressWarnings("unused")
		ColorMixIn(@JsonProperty("red") final double red,
				@JsonProperty("green") final double green,
				@JsonProperty("blue") final double blue,
				@JsonProperty("opacity") final double opacity) {
			// no implementation as this is a mix in only
		}

		@JsonIgnore
		abstract double getHue();

		@JsonIgnore
		abstract double getSaturation();

		@JsonIgnore
		abstract double getBrightness();

		@JsonIgnore
		abstract boolean isOpaque();
	}

	@Getter
	@SuppressWarnings("PMD.DataClass")
	private static class ParsableLocalElection {
		LocalDistrictRoot district;

		LocalDate date;

		String name;

		Map<String, OptionalInt> population;

		Map<String, OptionalInt> numberOfEligibleVoters;

		List<ParsableLocalNomination> nominations;

		int sainteLagueScale;

		Set<Party> parties;

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

		public void createPopulationFor(final LocalElection election) {
			final Map<String, OptionalInt> population = getPopulation();

			for (final District<?> district : election.getDistricts()) {
				election.setPopulation(district, population.getOrDefault(district.getKey(), OptionalInt.empty()));
			}
		}

		public void createNumberOfEligibleVotersFor(final LocalElection election) {
			final Map<String, OptionalInt> numberOfEligibleVoters = getNumberOfEligibleVoters();

			for (final District<?> district : election.getDistricts()) {
				election.setNumberOfEligibleVoters(district,
						numberOfEligibleVoters.getOrDefault(district.getKey(), OptionalInt.empty()));
			}
		}

		public void createNominationsFor(final LocalElection election) {
			final Map<String, District<?>> districts
					= election.getDistricts().stream().collect(toMap(District::getKey, Function.identity()));
			final Map<String, Party> parties = getParties().stream().collect(toMap(Party::getKey, Function.identity()));

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

	@Getter
	private static class ParsableLocalNomination {
		String district;

		Person person;

		Optional<String> party;

		private ParsableLocalNomination(final String district, final Person person, final Optional<String> party) {
			this.district = district;
			this.person = person;
			this.party = party;
		}
	}
}
