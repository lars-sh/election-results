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
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.utils.text.Patterns;
import de.larssh.utils.text.SplitLimit;
import de.larssh.utils.text.Strings;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LegacyParser {
	private static final Pattern BALLOT_PATTERN = Pattern.compile("^\\s*(?<count>\\d+)?\\s*(?<value>.*?)\\s*$");

	private static final Pattern NUMBER_OF_ALL_BALLOTS_PATTERN
			= Pattern.compile("^(?i)\\s*Anzahl\\s*Stimmzettel\\s*:?\\s*(?<value>\\d+)\\s*$");

	private static Collection<LocalBallot> createBallotFromLine(final LocalElection election,
			final LocalPollingStation pollingStation,
			final String line) {
		final Matcher matcher = Patterns.matches(BALLOT_PATTERN, line)
				.orElseThrow(() -> new ElectionException("Failed parsing line \"%s\".", line));

		LocalBallot ballot;
		if (matcher.group("value").equals("-")) {
			ballot = LocalBallot.createInvalidBallot(election, pollingStation, false);
		} else {
			final Set<LocalNomination> nominations
					= Arrays.stream(matcher.group("value").split("\\s+", SplitLimit.NONE))
							.map(nomination -> findNomination(election, pollingStation.getParent().get(), nomination))
							.collect(toSet());
			ballot = LocalBallot.createValidBallot(election, pollingStation, false, nominations);
		}

		final int count = matcher.group("count") == null ? 1 : Integer.parseInt(matcher.group("count"));
		return IntStream.range(0, count).mapToObj(index -> ballot).collect(toList());
	}

	private static LocalNomination findNomination(final LocalElection election,
			final LocalDistrict district,
			final String person) {
		if (person.isEmpty()) {
			throw new ElectionException("Cannot find a nomination for an empty string in district \"%s\".",
					district.getName());
		}

		final char possiblePartyIndicator = Character.toLowerCase(person.charAt(0));
		Set<LocalNomination> nominations = election.getNominations()
				.stream()
				.filter(nomination -> nomination.getDistrict().equals(district))
				.filter(nomination -> nomination.getType() == LocalNominationType.DIRECT)
				.filter(nomination -> nomination.getParty().isPresent())
				.filter(nomination -> !nomination.getParty().get().getShortName().isEmpty())
				.filter(nomination -> possiblePartyIndicator == Character
						.toLowerCase(nomination.getParty().get().getShortName().charAt(0)))
				.filter(nomination -> matches(nomination, person.substring(1)))
				.collect(toSet());
		if (nominations.size() == 1) {
			return nominations.iterator().next();
		}

		nominations = election.getNominations() // TODO: final
				.stream()
				.filter(nomination -> nomination.getDistrict().equals(district))
				.filter(nomination -> nomination.getType() == LocalNominationType.DIRECT)
				.filter(nomination -> matches(nomination, person))
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
		final Pattern pattern = Pattern.compile(getSimplifiedString(person).chars()
				.mapToObj(v -> Character.toString((char) v))
				.collect(joining(".*", "^.*", ".*$"))); // TODO: PatternCache
		return Strings.matches(getSimplifiedString(value), pattern);
	}

	private static String getSimplifiedString(final String value) {
		return value.toLowerCase()
				.replace("ä", "ae")
				.replace("ö", "oe")
				.replace("ü", "ue")
				.replace("ß", "ss")
				.replaceAll("[^a-z0-9]", "");
	}

	public static LocalElectionResult parse(final LocalElection election,
			final LocalPollingStation pollingStation,
			final Reader reader) throws IOException {
		try (final BufferedReader bufferedReader = new BufferedReader(reader)) {
			final AtomicReference<OptionalInt> numberOfAllBallots = new AtomicReference<>(OptionalInt.empty());
			final List<LocalBallot> ballots = new ArrayList<>();
			bufferedReader.lines()
					.map(String::trim)
					.filter(line -> !line.isEmpty() && !line.startsWith("#"))
					.forEachOrdered(line -> {
						if (line.startsWith("*")) {
							final String command = Strings.trimStart(line.substring(1));

							if ("CLEAR".equalsIgnoreCase(command)) {
								ballots.clear();
							}
							Patterns.matches(NUMBER_OF_ALL_BALLOTS_PATTERN, command).ifPresent(matcher -> {
								numberOfAllBallots.set(OptionalInt.of(Integer.parseInt(matcher.group("value"))));
							});
						} else {
							ballots.addAll(createBallotFromLine(election, pollingStation, line));
						}
					});
			return new LocalElectionResult(election, numberOfAllBallots.get(), ballots);
		}
	}
}
