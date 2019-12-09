package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.ElectionResult;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.collection.Maps;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LocalElectionResult implements ElectionResult<LocalBallot> {
	LocalElection election;

	List<LocalBallot> ballots;

	Set<LocalNominationResult> nominationResults;

	@PackagePrivate
	LocalElectionResult(final LocalElection election, final List<LocalBallot> ballots) {
		this(election, ballots, isEqual(true));
	}

	private LocalElectionResult(final LocalElection election,
			final List<LocalBallot> ballots,
			final Predicate<LocalBallot> filter) {
		this.election = election;
		this.ballots = unmodifiableList(ballots.stream().filter(filter).collect(toList()));
		nominationResults = unmodifiableSet(calculateNominationResults());
	}

	private Set<LocalNominationResult> calculateNominationResults() {
		final Map<LocalNomination, Integer> unsortedVotes = getBallots().stream()
				.filter(Ballot::isValid)
				.map(LocalBallot::getNominations)
				.flatMap(Set::stream)
				.collect(toMap(Function.identity(), nomination -> 1, (a, b) -> a + b));
		final Map<LocalNomination, Integer> votes = Maps.sort(unsortedVotes,
				Comparator.<Entry<LocalNomination, Integer>, Integer>comparing(Entry::getValue)
						.thenComparing(Entry::getKey));

		final Map<LocalNomination, LocalNominationResultType> types = new HashMap<>();

		// TODO: not 6!
		votes.keySet().stream().limit(6).forEach(nomination -> types.put(nomination, LocalNominationResultType.DIRECT));
		final OptionalInt lastDirect = votes.values().stream().skip(6 - 1).mapToInt(i -> i).findFirst();
		if (lastDirect.isPresent()) {
			final List<LocalNomination> probablyDrawSeats = votes.entrySet()
					.stream()
					.filter(entry -> entry.getValue() == lastDirect.getAsInt())
					.map(Entry::getKey)
					.collect(toList());
			if (probablyDrawSeats.size() > 1) {
				probablyDrawSeats.forEach(nomination -> types.put(nomination, LocalNominationResultType.DRAW));
			}
		}

		return getElection().getNominations()
				.stream()
				.map(nomination -> new LocalNominationResult(this,
						nomination,
						getBallots().stream()
								.filter(ballot -> ballot.getNominations().contains(nomination))
								.collect(toList()),
						types.getOrDefault(nomination, LocalNominationResultType.NOT_ELECTED)))
				.collect(toCollection(TreeSet::new));
	}

	//
	// public Map<Kandidat, BigDecimal> getSainteLague() {
	// Map<Kandidat, BigDecimal> sainteLague =
	// getStimmen(Ergebnistyp.GRUPPIERUNG).entrySet().stream() //
	// .map(e -> getSainteLague(e.getKey(), e.getValue())) //
	// .map(Map::entrySet) //
	// .flatMap(Set::stream) //
	// .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
	// Utils.throwingMerger(), LinkedHashMap::new));
	//
	// Map<Kandidat, Integer> stimmen = getStimmen(Ergebnistyp.KANDIDAT);
	// List<Kandidat> kandidaten = new
	// ArrayList<>(getKommunalwahl().getKandidaten());
	// Comparator<Entry<Kandidat, BigDecimal>> comparator = (a, b) ->
	// b.getValue().compareTo(a.getValue());
	// comparator.thenComparing((a, b) -> stimmen.get(a.getKey()) -
	// stimmen.get(b.getKey())); // TODO: a - b validieren
	// comparator = comparator
	// .thenComparing((a, b) -> kandidaten.indexOf(a.getKey()) -
	// kandidaten.indexOf(b.getKey()));
	// return Utils.sortMap(sainteLague, comparator);
	// }
	//
	// private Map<Kandidat, BigDecimal> getSainteLague(Gruppierung gruppierung, int
	// anzahlStimmenGruppierung) {
	// Objects.requireNonNull(gruppierung, "Gruppierung");
	//
	// Iterator<Kandidat> kandidaten =
	// getSainteLagueKandidaten(gruppierung).iterator();
	//
	// return IntStream.range(0, gruppierung.getKandidaten().size()) //
	// .mapToObj(i -> BigDecimal.valueOf(anzahlStimmenGruppierung) //
	// .divide(BigDecimal.valueOf(i * 10 + 5, 1),
	// getKommunalwahl().getPrecisionSainteLague(),
	// RoundingMode.HALF_UP)) //
	// .collect(Collectors.toMap(i -> kandidaten.next(), Function.identity(),
	// Utils.throwingMerger(),
	// LinkedHashMap::new));
	// }
	//
	// private Set<Kandidat> getSainteLagueKandidaten(Gruppierung gruppierung) {
	// Objects.requireNonNull(gruppierung, "Gruppierung");
	//
	// // Direktmandate
	// Set<Kandidat> kandidaten = getDirektmandate().stream() //
	// .filter(k -> k.getGruppierung() == gruppierung) //
	// .collect(Utils.toLinkedHashSet());
	//
	// kandidaten.addAll(gruppierung.getKandidaten());
	//
	// return kandidaten;
	// }
	//
	// public Set<Kandidat> getMandate() {
	// Set<Kandidat> mandate = new LinkedHashSet<>();
	// for (Kandidat kandidat : getSainteLague().keySet()) {
	// if (mandate.size() >= getKommunalwahl().getAnzahlSitze() &&
	// mandate.containsAll(getDirektmandate())) {
	// return mandate;
	// }
	// mandate.add(kandidat);
	// }
	// return mandate;
	// }
	//
	// public Map<Gruppierung, Integer> getSitze() {
	// Map<Gruppierung, Integer> sitze = getMandate().stream() //
	// .collect(Collectors.toMap(Kandidat::getGruppierung, k -> 1, Integer::sum));
	//
	// List<Gruppierung> gruppierungen = new
	// ArrayList<>(getKommunalwahl().getGruppierungen());
	// for (Gruppierung gruppierung : gruppierungen) {
	// sitze.putIfAbsent(gruppierung, 0);
	// }
	//
	// Map<Gruppierung, Integer> stimmen = getStimmen(Ergebnistyp.GRUPPIERUNG);
	// Comparator<Entry<Gruppierung, Integer>> comparator = (a, b) ->
	// b.getValue().compareTo(a.getValue());
	// comparator.thenComparing((a, b) -> stimmen.get(a.getKey()) -
	// stimmen.get(b.getKey())); // TODO: a - b validieren
	// comparator.thenComparing((a, b) -> gruppierungen.indexOf(a.getKey()) -
	// gruppierungen.indexOf(b.getKey())); // TODO:
	// // a
	// // -
	// // b
	// // validieren
	// return Utils.sortMap(sitze, comparator);
	// }

	@Override
	public ElectionResult<LocalBallot> filter(final Predicate<LocalBallot> filter) {
		return new LocalElectionResult(getElection(), getBallots(), filter);
	}
}
