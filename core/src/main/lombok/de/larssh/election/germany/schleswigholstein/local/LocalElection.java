package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toMap;
import static de.larssh.utils.Finals.constant;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public class LocalElection implements Election {
	private static final ObjectMapper JACKSON_OBJECT_MAPPER = new ObjectMapper() //
			.registerModule(new JavaTimeModule())
			.registerModule(new Jdk8Module())
			.registerModule(new ParameterNamesModule());

	private static final NavigableMap<Integer, Integer> NUMBER_OF_DIRECT_SEATS;

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
		NUMBER_OF_DIRECT_SEATS = unmodifiableNavigableMap(numberOfDirectSeats);
	}

	public static ObjectMapper getJacksonObjectMapper() {
		return JACKSON_OBJECT_MAPPER;
	}

	LocalDistrictRoot district;

	@EqualsAndHashCode.Include
	LocalDate date;

	@EqualsAndHashCode.Include
	String name;

	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> population = new HashMap<>();

	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> numberOfEligibleVoters = new HashMap<>();

	List<LocalNomination> nominations = new ArrayList<>();

	int sainteLagueScale;

	Supplier<Set<District<?>>> districts = lazy(() -> {
		Set<District<?>> districts = new HashSet<>();

		// Root
		districts.add(getDistrict());

		// Districts
		districts.addAll(getDistrict().getChildren());

		// Polling Stations
		for (LocalDistrict district : getDistrict().getChildren()) {
			districts.addAll(district.getChildren());
		}

		return unmodifiableSet(districts);
	});

	public LocalElection(ParsableLocalElection parsable) {
		this(parsable.getDistrict(), parsable.getDate(), parsable.getName(), parsable.getSainteLagueScale());

		parsable.createPopulationFor(this);
		parsable.createNumberOfEligibleVotersFor(this);
		parsable.createNominationsFor(this);
	}

	@Override
	public OptionalInt getPopulation(final District<?> district) {
		return Nullables.orElseGet(population.get(district), OptionalInt::empty);
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
		return NUMBER_OF_DIRECT_SEATS.floorEntry(population - 1).getValue();
	}

	@JsonIgnore
	public int getNumberOfVotes() {
		return getNumberOfDirectSeats();
	}

	@Override
	public OptionalInt getNumberOfEligibleVoters(final District<?> district) {
		return Nullables.orElseGet(numberOfEligibleVoters.get(district), OptionalInt::empty);
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
		this.numberOfEligibleVoters.put(district, numberOfEligibleVoters);
	}

	public LocalNomination createNomination(final LocalDistrict district,
			final LocalNominationType type,
			final Person person,
			final Optional<Party> party) {
		final LocalNomination nomination = new LocalNomination(this, district, type, person, party);
		nominations.add(nomination);
		return nomination;
	}

	@Override
	public List<LocalNomination> getNominations() {
		return unmodifiableList(nominations);
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

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalElection {
		LocalDistrictRoot district;

		LocalDate date;

		String name;

		Map<String, OptionalInt> population;

		Map<String, OptionalInt> numberOfEligibleVoters;

		List<ParsableLocalNomination> nominations;

		int sainteLagueScale;

		Set<Party> parties;

		public void createPopulationFor(LocalElection election) {
			Map<String, OptionalInt> population = getPopulation();

			for (District<?> district : election.getDistricts()) {
				election.setPopulation(district, population.getOrDefault(district.getName(), OptionalInt.empty()));
			}
		}

		public void createNumberOfEligibleVotersFor(LocalElection election) {
			Map<String, OptionalInt> numberOfEligibleVoters = getNumberOfEligibleVoters();

			for (District<?> district : election.getDistricts()) {
				election.setNumberOfEligibleVoters(district,
						numberOfEligibleVoters.getOrDefault(district.getName(), OptionalInt.empty()));
			}
		}

		public void createNominationsFor(LocalElection election) {
			Map<String, District<?>> districts
					= election.getDistricts().stream().collect(toMap(District::getName, Function.identity()));
			Map<String, Party> parties = getParties().stream().collect(toMap(Party::getShortName, Function.identity()));

			for (ParsableLocalNomination nomination : getNominations()) {
				District<?> district = districts.get(nomination.getDistrict());
				if (district == null) {
					throw new ElectionException("District \"%s\" of nomination \"%s, %s\" does not exist.",
							nomination.getDistrict(),
							nomination.getPerson().getGivenName(),
							nomination.getPerson().getFamilyName());
				}
				if (!(district instanceof LocalDistrict)) {
					throw new ElectionException(
							"District \"%s\" of nomination \"%s, %s\" is of type %s. Expecting type %s for nominations.",
							nomination.getDistrict(),
							nomination.getPerson().getGivenName(),
							nomination.getPerson().getFamilyName(),
							district.getClass().getSimpleName(),
							LocalDistrict.class.getSimpleName());
				}

				Optional<Party> party = nomination.getParty().map(parties::get);
				if (nomination.getParty().isPresent() && !party.isPresent()) {
					throw new ElectionException("Party \"%s\" of nomination \"%s, %s\" does not exist.",
							nomination.getParty().get(),
							nomination.getPerson().getGivenName(),
							nomination.getPerson().getFamilyName());
				}

				election.createNomination((LocalDistrict) district,
						nomination.getType(),
						nomination.getPerson(),
						party);
			}
		}
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalNomination {
		String district;

		LocalNominationType type;

		Person person;

		Optional<String> party;
	}
}
