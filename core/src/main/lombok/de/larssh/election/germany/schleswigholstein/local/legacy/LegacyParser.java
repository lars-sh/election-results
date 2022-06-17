package de.larssh.election.germany.schleswigholstein.local.legacy;

import static java.util.Collections.emptySet;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.local.LocalBallot;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNomination;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationType;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.text.Patterns;
import de.larssh.utils.text.SplitLimit;
import de.larssh.utils.text.Strings;
import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods to parse legacy file formats.
 */
@UtilityClass
@SuppressWarnings("PMD.ClassNamingConventions")
public class LegacyParser {
	/**
	 * Invalid ballot character
	 */
	private static final String BALLOT_INVALID = "-";

	/**
	 * Name of the pattern group for the optional count of a ballot line
	 */
	private static final String GROUP_COUNT = "count";

	/**
	 * Name of the pattern group for the value of a ballot line
	 */
	private static final String GROUP_VALUE = "value";

	/**
	 * Pattern to parse a ballot line
	 */
	private static final Pattern BALLOT_PATTERN
			= Pattern.compile("^\\s*(?<" + GROUP_COUNT + ">\\d+)?\\s*(?<" + GROUP_VALUE + ">.*?)\\s*$");

	/**
	 * Command to be used to clear all election results
	 */
	private static final String COMMAND_CLEAR = "clear";

	/**
	 * Pattern to match lines stating the number of all ballots
	 */
	private static final Pattern NUMBER_OF_ALL_BALLOTS_PATTERN
			= Pattern.compile("^(?i)Anzahl\\s+Stimmzettel\\s*:?\\s*(?<" + GROUP_VALUE + ">\\d+)$");

	/**
	 * Character to start command lines with
	 */
	private static final char LINE_COMMAND = '*';

	/**
	 * Character to start line comments with
	 */
	private static final char LINE_COMMENT = '#';

	/**
	 * Cache for patterns to match persons
	 */
	private static final Map<String, Pattern> PATTERN_CACHE = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Parses a single {@code line} and creates ballots out of it.
	 *
	 * @param election       Wahl
	 * @param pollingStation Wahlbezirk
	 * @param line           single line
	 * @return Stimmzettel
	 */
	private static Collection<LocalBallot> createBallotsFromLine(final LocalElection election,
			final LocalPollingStation pollingStation,
			final String line) {
		final Matcher matcher = Patterns.matches(BALLOT_PATTERN, line)
				.orElseThrow(() -> new ElectionException("Failed parsing line \"%s\".", line));

		final LocalBallot ballot;
		if (BALLOT_INVALID.equals(matcher.group(GROUP_VALUE))) {
			ballot = LocalBallot.createInvalidBallot(election, pollingStation, false);
		} else {
			final Set<LocalNomination> nominations
					= Arrays.stream(matcher.group(GROUP_VALUE).split("\\s+", SplitLimit.NO_LIMIT))
							.map(nomination -> findNomination(election, pollingStation.getParent().get(), nomination))
							.collect(toSet());
			ballot = LocalBallot.createValidBallot(election, pollingStation, false, nominations);
		}

		final int count = Optional.ofNullable(matcher.group(GROUP_COUNT)).map(Integer::parseInt).orElse(1);
		return IntStream.range(0, count).mapToObj(index -> ballot).collect(toList());
	}

	/**
	 * Determines the correct nomination for {@code person}.
	 *
	 * <p>
	 * This method takes care of the party prefix.
	 *
	 * @param election Wahl
	 * @param district Wahlkreis
	 * @param person   Bewerberin oder Bewerber
	 * @return Bewerberin oder Bewerber
	 */
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

	/**
	 * Checks if {@code nomination} is the correct nomination for {@code person}.
	 *
	 * <p>
	 * This method makes sure, that both, the given and the family name can be in
	 * either first or second place of order.
	 *
	 * @param nomination Bewerberin oder Bewerber
	 * @param person     Bewerberin oder Bewerber
	 * @return {@code true} if {@code nomination} is the correct nomination for
	 *         {@code person}, else {@code false}
	 */
	private static boolean matches(final LocalNomination nomination, final String person) {
		final String familyName = nomination.getPerson().getFamilyName();
		final String givenName = nomination.getPerson().getGivenName();
		return matches(givenName + familyName, person) || matches(familyName + givenName, person);
	}

	/**
	 * Checks if {@code person} matches {@code value}.
	 *
	 * @param value  the pattern
	 * @param person Bewerberin oder Bewerber
	 * @return {@code true} if {@code person} matches {@code value}, else
	 *         {@code false}
	 */
	private static boolean matches(final String value, final String person) {
		final Pattern pattern = PATTERN_CACHE.computeIfAbsent(person,
				key -> Pattern.compile(getSimplifiedString(key).chars()
						.mapToObj(v -> Character.toString((char) v))
						.collect(joining(".*", "^.*", ".*$"))));
		return Strings.matches(getSimplifiedString(value), pattern);
	}

	/**
	 * Reduces the complexity of {@code value} by converting to lower case
	 * characters, replacing umlauts and removing characters not matching a-z and
	 * 0-9.
	 *
	 * @param value the value to simplify
	 * @return the simplified value
	 */
	private static String getSimplifiedString(final String value) {
		return Strings.toLowerCaseNeutral(value)
				.replace("ä", "ae")
				.replace("ö", "oe")
				.replace("ü", "ue")
				.replace("ß", "ss")
				.replaceAll("[^a-z0-9]", "");
	}

	/**
	 * Parses a legacy file format for a specific {@code polllingStation}.
	 *
	 * @param election       Wahl
	 * @param pollingStation Wahlbezirk
	 * @param reader         legacy file input
	 * @return the new result object
	 * @throws IOException on IO error
	 */
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
							final String command = line.substring(1).trim();

							if (Strings.equalsIgnoreCaseAscii(COMMAND_CLEAR, command)) {
								ballots.clear();
							} else {
								Patterns.matches(NUMBER_OF_ALL_BALLOTS_PATTERN, command)
										.ifPresent(matcher -> numberOfAllBallots
												.set(OptionalInt.of(Integer.parseInt(matcher.group(GROUP_VALUE)))));
							}
						} else {
							ballots.addAll(createBallotsFromLine(election, pollingStation, line));
						}
					});
			return new LocalElectionResult(election, numberOfAllBallots.get(), ballots, emptySet(), emptySet());
		} finally {
			PATTERN_CACHE.clear();
		}
	}
}
