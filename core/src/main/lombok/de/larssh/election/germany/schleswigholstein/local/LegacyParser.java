package de.larssh.election.germany.schleswigholstein.local;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.utils.text.Patterns;
import de.larssh.utils.text.SplitLimit;
import de.larssh.utils.text.Strings;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LegacyParser {
	private static final String BALLOT_INVALID = "-";

	private static final String GROUP_COUNT = "count";

	private static final String GROUP_VALUE = "value";

	private static final Pattern BALLOT_PATTERN
			= Pattern.compile("^\\s*(?<" + GROUP_COUNT + ">\\d+)?\\s*(?<" + GROUP_VALUE + ">.*?)\\s*$");

	private static final String COMMAND_CLEAR = "clear";

	private static final Pattern NUMBER_OF_ALL_BALLOTS_PATTERN
			= Pattern.compile("^(?i)\\s*Anzahl\\s*Stimmzettel\\s*:?\\s*(?<" + GROUP_VALUE + ">\\d+)\\s*$");

	private static final char LINE_COMMAND = '*';

	private static final char LINE_COMMENT = '#';

	private static final Map<String, Pattern> PATTERN_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

	private static Collection<LocalBallot> createBallotFromLine(final LocalElection election,
			final LocalPollingStation pollingStation,
			final String line) {
		final Matcher matcher = Patterns.matches(BALLOT_PATTERN, line)
				.orElseThrow(() -> new ElectionException("Failed parsing line \"%s\".", line));

		final LocalBallot ballot;
		if (BALLOT_INVALID.equals(matcher.group(GROUP_VALUE))) {
			ballot = LocalBallot.createInvalidBallot(election, pollingStation, false);
		} else {
			final Set<LocalNomination> nominations
					= Arrays.stream(matcher.group(GROUP_VALUE).split("\\s+", SplitLimit.NONE))
							.map(nomination -> findNomination(election, pollingStation.getParent().get(), nomination))
							.collect(toSet());
			ballot = LocalBallot.createValidBallot(election, pollingStation, false, nominations);
		}

		final int count = Optional.ofNullable(matcher.group(GROUP_COUNT)).map(Integer::parseInt).orElse(1);
		return IntStream.range(0, count).mapToObj(index -> ballot).collect(toList());
	}

	private static LocalNomination findNomination(final Election<?, ? extends LocalNomination> election,
			final LocalDistrict district,
			final String person) {
		if (person.isEmpty()) {
			throw new ElectionException("Cannot find a nomination for an empty string in district \"%s\".",
					district.getName());
		}

		final char possiblePartyIndicator = Character.toLowerCase(person.charAt(0));
		final Set<LocalNomination> nominationsWithPartyPrefix = election.getNominations()
				.stream()
				.filter(nomination -> nomination.getDistrict().equals(district)
						&& nomination.getType() == LocalNominationType.DIRECT
						&& nomination.getParty().isPresent()
						&& !nomination.getParty().get().getShortName().isEmpty()
						&& possiblePartyIndicator == Character
								.toLowerCase(nomination.getParty().get().getShortName().charAt(0))
						&& matches(nomination, person.substring(1)))
				.collect(toSet());
		if (nominationsWithPartyPrefix.size() == 1) {
			return nominationsWithPartyPrefix.iterator().next();
		}

		final Set<LocalNomination> nominations = election.getNominations()
				.stream()
				.filter(nomination -> nomination.getDistrict().equals(district)
						&& nomination.getType() == LocalNominationType.DIRECT
						&& matches(nomination, person))
				.collect(toSet());
		if (nominations.isEmpty()) {
			throw new ElectionException("Cannot find a nomination for \"%s\" in district \"%s\".",
					person,
					district.getName());
		}
		if (nominations.size() > 1) {
			throw new ElectionException("Found %d nominations for \"%s\" in district \"%s\": \"%s\"",
					nominations.size(),
					person,
					district.getName(),
					nominations.stream().map(LocalNomination::getKey).collect(joining("\", \"")));
		}
		return nominations.iterator().next();
	}

	private static boolean matches(final LocalNomination nomination, final String person) {
		final String familyName = nomination.getPerson().getFamilyName();
		final String givenName = nomination.getPerson().getGivenName();
		return matches(givenName + familyName, person) || matches(familyName + givenName, person);
	}

	private static boolean matches(final String value, final String person) {
		final Pattern pattern = PATTERN_CACHE.computeIfAbsent(person,
				key -> Pattern.compile(getSimplifiedString(key).chars()
						.mapToObj(v -> Character.toString((char) v))
						.collect(joining(".*", "^.*", ".*$"))));
		return Strings.matches(getSimplifiedString(value), pattern);
	}

	public static LocalElectionResult mergeResults(final LocalElection election, final LocalElectionResult... results) {
		final OptionalInt numberOfAllBallots;
		if (Arrays.stream(results).map(LocalElectionResult::getNumberOfAllBallots).allMatch(OptionalInt::isPresent)) {
			numberOfAllBallots = OptionalInt.of(Arrays.stream(results)
					.map(LocalElectionResult::getNumberOfAllBallots)
					.mapToInt(OptionalInt::getAsInt)
					.sum());
		} else {
			numberOfAllBallots = OptionalInt.empty();
		}

		final List<LocalBallot> ballots = new ArrayList<>();
		Arrays.stream(results).map(LocalElectionResult::getBallots).forEach(ballots::addAll);
		return new LocalElectionResult(election, numberOfAllBallots, ballots);
	}

	private static String getSimplifiedString(final String value) {
		return Strings.toNeutralLowerCase(value)
				.replace("ä", "ae")
				.replace("ö", "oe")
				.replace("ü", "ue")
				.replace("ß", "ss")
				.replaceAll("[^a-z0-9]", "");
	}

	public static LocalElectionResult parse(final LocalElection election,
			final LocalPollingStation pollingStation,
			final Reader reader) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(reader)) {
			final AtomicReference<OptionalInt> numberOfAllBallots = new AtomicReference<>(OptionalInt.empty());
			final List<LocalBallot> ballots = new ArrayList<>();
			bufferedReader.lines()
					.map(String::trim)
					.filter(line -> !line.isEmpty() && line.charAt(0) != LINE_COMMENT)
					.forEachOrdered(line -> {
						if (line.charAt(0) == LINE_COMMAND) {
							final String command = Strings.trimStart(line.substring(1));

							if (COMMAND_CLEAR.equalsIgnoreCase(command)) {
								ballots.clear();
							}
							Patterns.matches(NUMBER_OF_ALL_BALLOTS_PATTERN, command)
									.ifPresent(matcher -> numberOfAllBallots
											.set(OptionalInt.of(Integer.parseInt(matcher.group(GROUP_VALUE)))));
						} else {
							ballots.addAll(createBallotFromLine(election, pollingStation, line));
						}
					});
			return new LocalElectionResult(election, numberOfAllBallots.get(), ballots);
		}
	}
}
