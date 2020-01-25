package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toMap;
import static de.larssh.utils.Finals.constant;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
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
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.paint.Color;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(onConstructor_ = { @JsonIgnore })
@EqualsAndHashCode(onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public class LocalElection implements Election {
	private static final NavigableMap<Integer, Integer> DIRECT_SEATS_PER_POPULATION;

	public static final int SAINTE_LAGUE_SCALE_DEFAULT = constant(2);

	static {
		final NavigableMap<Integer, Integer> numberOfDirectSeats = new TreeMap<>();
		numberOfDirectSeats.put(0, 3);
		numberOfDirectSeats.put(200, 4);
		numberOfDirectSeats.put(750, 5);
		numberOfDirectSeats.put(1250, 6);
		numberOfDirectSeats.put(2500, 8);
		numberOfDirectSeats.put(5000, 9);
		numberOfDirectSeats.put(10_000, 11);
		numberOfDirectSeats.put(15_000, 13);
		numberOfDirectSeats.put(25_000, 15);
		numberOfDirectSeats.put(35_000, 17);
		numberOfDirectSeats.put(45_000, 19);
		DIRECT_SEATS_PER_POPULATION = unmodifiableNavigableMap(numberOfDirectSeats);
	}

	public static ObjectMapper createJacksonObjectMapper() {
		return new ObjectMapper() //
				.addMixIn(Color.class, ColorMixIn.class)
				.registerModule(new JavaTimeModule())
				.registerModule(new Jdk8Module())
				.registerModule(new ParameterNamesModule());
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
	private Map<String, OptionalInt> getPopulationForJackson() {
		return population.entrySet().stream().collect(toMap(entry -> entry.getKey().getName(), Entry::getValue));
	}

	@Override
	public void setPopulation(final District<?> district, final OptionalInt population) {
		if (!district.getRoot().equals(getDistrict())) {
			throw new ElectionException("District \"%s\" is not part of the elections district hierarchy.",
					district.getName());
		}
		if (this.population.containsKey(district)) {
			throw new ElectionException("The population has already been set for district \"%s\".", district.getName());
		}
		this.population.put(district, population);
	}

	@JsonIgnore
	public int getNumberOfSeats() {
		return 2 * getNumberOfListSeats() + 1;
	}

	@JsonIgnore
	public int getNumberOfDirectSeats() {
		return getNumberOfListSeats() + 1;
	}

	@JsonIgnore
	public int getNumberOfListSeats() {
		final int population = getPopulation();

		if (getDistrict().getType() == LocalDistrictType.KREIS) {
			return population > 200_000 ? 24 : 22;
		}
		if (getDistrict().getType() == LocalDistrictType.KREISFREIE_STADT) {
			return population > 150_000 ? 24 : 21;
		}
		return DIRECT_SEATS_PER_POPULATION.floorEntry(population - 1).getValue();
	}

	@JsonIgnore
	public int getNumberOfVotes() {
		return getNumberOfDirectSeats();
	}

	@Override
	public OptionalInt getNumberOfEligibleVoters(final District<?> district) {
		final OptionalInt numberOfEligibleVoters = this.numberOfEligibleVoters.get(district);
		if (numberOfEligibleVoters != null) {
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
	private Map<String, OptionalInt> getNumberOfEligibleVotersForJackson() {
		return numberOfEligibleVoters.entrySet()
				.stream()
				.collect(toMap(entry -> entry.getKey().getName(), Entry::getValue));
	}

	@Override
	public void setNumberOfEligibleVoters(final District<?> district, final OptionalInt numberOfEligibleVoters) {
		if (!district.getRoot().equals(getDistrict())) {
			throw new ElectionException("District \"%s\" is not part of the elections district hierarchy.",
					district.getName());
		}
		if (this.numberOfEligibleVoters.containsKey(district)) {
			throw new ElectionException("The number of eligible voters has already been set for district \"%s\".",
					district.getName());
		}
		this.numberOfEligibleVoters.put(district, numberOfEligibleVoters);
	}

	public LocalNomination createNomination(final LocalDistrict district,
			final Person person,
			final Optional<Party> party) {
		final LocalNomination nomination = new LocalNomination(this, district, party, person);
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

	public Set<Party> getParties() {
		return getNominations().stream()
				.map(Nomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toCollection(TreeSet::new));
	}

	private abstract static class ColorMixIn {
		@SuppressWarnings("unused")
		public ColorMixIn(@JsonProperty("red") final double red,
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
	private static class ParsableLocalElection {
		LocalDistrictRoot district;

		LocalDate date;

		String name;

		Map<String, OptionalInt> population;

		Map<String, OptionalInt> numberOfEligibleVoters;

		List<ParsableLocalNomination> nominations;

		int sainteLagueScale;

		Set<Party> parties;

		@SuppressWarnings("unused")
		public ParsableLocalElection(@Nullable final LocalDistrictRoot district,
				@Nullable final LocalDate date,
				@Nullable final String name,
				@Nullable final Map<String, OptionalInt> population,
				@Nullable final Map<String, OptionalInt> numberOfEligibleVoters,
				@Nullable final List<de.larssh.election.germany.schleswigholstein.local.LocalElection.ParsableLocalNomination> nominations,
				@Nullable final Integer sainteLagueScale,
				@Nullable final Set<Party> parties) {
			this.district = Nullables.orElseThrow(district);
			this.date = Nullables.orElseThrow(date);
			this.name = Nullables.orElseThrow(name);
			this.population = Nullables.orElseGet(population, Collections::emptyMap);
			this.numberOfEligibleVoters = Nullables.orElseGet(numberOfEligibleVoters, Collections::emptyMap);
			this.nominations = Nullables.orElseGet(nominations, Collections::emptyList);
			this.sainteLagueScale = Nullables.orElse(sainteLagueScale, 2);
			this.parties = Nullables.orElseGet(parties, Collections::emptySet);
		}

		public void createPopulationFor(final LocalElection election) {
			final Map<String, OptionalInt> population = getPopulation();

			for (final District<?> district : election.getDistricts()) {
				election.setPopulation(district, population.getOrDefault(district.getName(), OptionalInt.empty()));
			}
		}

		public void createNumberOfEligibleVotersFor(final LocalElection election) {
			final Map<String, OptionalInt> numberOfEligibleVoters = getNumberOfEligibleVoters();

			for (final District<?> district : election.getDistricts()) {
				election.setNumberOfEligibleVoters(district,
						numberOfEligibleVoters.getOrDefault(district.getName(), OptionalInt.empty()));
			}
		}

		public void createNominationsFor(final LocalElection election) {
			final Map<String, District<?>> districts
					= election.getDistricts().stream().collect(toMap(District::getName, Function.identity()));
			final Map<String, Party> parties
					= getParties().stream().collect(toMap(Party::getShortName, Function.identity()));

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
	@RequiredArgsConstructor
	private static class ParsableLocalNomination {
		String district;

		Person person;

		Optional<String> party;
	}
}
