package de.larssh.election.germany.schleswigholstein.local.file;

import static de.larssh.utils.Collectors.toLinkedHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalBallot;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNomination;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationType;
import de.larssh.election.germany.schleswigholstein.local.LocalPartyResult;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.collection.Maps;
import de.larssh.utils.text.Patterns;
import de.larssh.utils.text.SplitLimit;
import de.larssh.utils.text.Strings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods to read and write the polling station's
 * result file format.
 */
@UtilityClass
@SuppressWarnings("PMD.ExcessiveImports")
public class PollingStationResultFiles {
	/**
	 * Invalid ballot character
	 */
	private static final String BALLOT_INVALID = "-";

	/**
	 * Command to be used to clear all election results
	 */
	private static final String COMMAND_CLEAR = "clear";

	/**
	 * Character to start command lines with
	 */
	private static final char LINE_COMMAND = '*';

	/**
	 * Character to start line comments with
	 */
	private static final char LINE_COMMENT = '#';

	/**
	 * Parses the polling station's file format.
	 *
	 * @param election       Wahl
	 * @param pollingStation Wahlbezirk
	 * @param reader         polling station file input
	 * @return the new result object
	 * @throws IOException on IO error
	 */
	public static LocalElectionResult read(final LocalElection election,
			final LocalPollingStation pollingStation,
			final Reader reader) throws IOException {
		return new PollingStationResultFileReader(election, pollingStation, reader).read();
	}

	/**
	 * Formats and writes the {@code result} of {@code pollingStation} to
	 * {@code writer}.
	 *
	 * @param result         the election result to to write
	 * @param pollingStation the polling station to write
	 * @param writer         the polling station result file writer
	 * @throws IOException on IO error
	 */
	public static void write(final LocalElectionResult result,
			final LocalPollingStation pollingStation,
			final Writer writer) throws IOException {
		new PollingStationResultFileWriter(result.filterByDistrict(pollingStation), pollingStation, writer).write();
	}

	/**
	 * This class reads data from a polling station result file to a
	 * {@link LocalElectionResult}.
	 */
	@RequiredArgsConstructor
	private static class PollingStationResultFileReader {
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
		 * Pattern to match lines stating the number of all ballots
		 */
		private static final Pattern NUMBER_OF_ALL_BALLOTS_PATTERN
				= Pattern.compile("^(?i)Anzahl\\s+Stimmzettel\\s*:?\\s*(?<" + GROUP_VALUE + ">\\d+)$");

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
		 * {@link LOcalElection} to write
		 *
		 * @return the election result to write
		 */
		LocalElection election;

		/**
		 * Polling Station to read
		 *
		 * @return the polling station to read
		 */
		LocalPollingStation pollingStation;

		/**
		 * Polling Station Result file read
		 *
		 * @return the polling station result file read
		 */
		Reader reader;

		/**
		 * Cache for patterns to match persons
		 */
		Map<String, Pattern> patternCache = new HashMap<>();

		/**
		 * Parsed number of all ballots
		 */
		AtomicReference<OptionalInt> numberOfAllBallots = new AtomicReference<>(OptionalInt.empty());

		/**
		 * Successfully parsed ballots
		 */
		List<LocalBallot> ballots = new ArrayList<>();

		/**
		 * Collected parse errors
		 */
		List<PollingStationResultFileLineParseException> exceptions = new ArrayList<>();

		/**
		 * Reads and parses {@link #reader} and creates a {@link LocalElectionResult}
		 * for {@link #election} in {@link #pollingStation}.
		 *
		 * @return the created {@link LocalElectionResult}
		 * @throws IOException on IO error
		 */
		public LocalElectionResult read() throws IOException {
			try (BufferedReader bufferedReader = new BufferedReader(reader)) {
				final AtomicInteger lineNumber = new AtomicInteger(0);
				bufferedReader.lines()
						.map(line -> Maps.entry(lineNumber.incrementAndGet(), line.trim()))
						.filter(entry -> !entry.getValue().isEmpty() && entry.getValue().charAt(0) != LINE_COMMENT)
						.forEachOrdered(entry -> parseLine(entry.getKey(), entry.getValue()));

				final LocalElectionResult result = new LocalElectionResult(election,
						2,
						Maps.<District<?>, OptionalInt>builder().put(pollingStation, numberOfAllBallots.get()).get(),
						emptySet(),
						emptySet(),
						ballots);
				if (exceptions.isEmpty()) {
					return result;
				}
				throw new PollingStationResultFileParseException(exceptions,
						result,
						"Failed parsing the polling station's result file.");
			}
		}

		/**
		 * Tries to parse {@code link}.
		 *
		 * @param lineNumber the line number
		 * @param line       the line content
		 */
		@SuppressWarnings({ "checkstyle:XIllegalCatchDefault", "PMD.AvoidCatchingGenericException" })
		private void parseLine(final int lineNumber, final String line) {
			// Ballots
			if (line.charAt(0) != LINE_COMMAND) {
				try {
					ballots.addAll(createBallotsFromLine(line));
				} catch (final Exception e) {
					exceptions.add(new PollingStationResultFileLineParseException(e, lineNumber, line, e.getMessage()));
				}
				return;
			}

			// Clear Command
			final String command = line.substring(1).trim();
			if (Strings.equalsIgnoreCaseAscii(COMMAND_CLEAR, command)) {
				ballots.clear();
				return;
			}

			// Number of all Ballots Command
			final Optional<Matcher> matcher = Patterns.matches(NUMBER_OF_ALL_BALLOTS_PATTERN, command);
			if (matcher.isPresent()) {
				numberOfAllBallots.set(OptionalInt.of(Integer.parseInt(matcher.get().group(GROUP_VALUE))));
				return;
			}

			exceptions.add(
					new PollingStationResultFileLineParseException(lineNumber, line, "Failed parsing command line."));
		}

		/**
		 * Parses a single {@code line} and creates ballots out of it.
		 *
		 * @param line the line to create a set of {@link LocalBallot} of
		 * @return Stimmzettel
		 */
		private Collection<LocalBallot> createBallotsFromLine(final String line) {
			final Matcher matcher = Patterns.matches(BALLOT_PATTERN, line)
					.orElseThrow(() -> new ElectionException("Failed parsing line \"%s\".", line));

			final LocalBallot ballot;
			if (BALLOT_INVALID.equals(matcher.group(GROUP_VALUE))) {
				ballot = LocalBallot.createInvalidBallot(election, pollingStation, false);
			} else {
				final Set<LocalNomination> nominations
						= Arrays.stream(matcher.group(GROUP_VALUE).split("\\s+", SplitLimit.NO_LIMIT))
								.map(this::findNomination)
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
		 * @param person Bewerberin oder Bewerber
		 * @return Bewerberin oder Bewerber
		 */
		private LocalNomination findNomination(final String person) {
			final LocalDistrict district = pollingStation.getDistrict();
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
		private boolean matches(final LocalNomination nomination, final String person) {
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
		private boolean matches(final String value, final String person) {
			final Pattern pattern = patternCache.computeIfAbsent(person,
					key -> Pattern.compile(getSimplifiedString(key).chars()
							.mapToObj(v -> Character.toString((char) v))
							.collect(joining(".*", "^.*", ".*$"))));
			return Strings.matches(getSimplifiedString(value), pattern);
		}
	}

	/**
	 * This class writes data of a {@link LocalElectionResult} to a polling station
	 * result file.
	 */
	@RequiredArgsConstructor
	private static class PollingStationResultFileWriter {
		/**
		 * Pattern matching exactly one space character
		 */
		private static final Pattern SINGLE_SPACE_PATTERN = Pattern.compile("\\s");

		/**
		 * Formats the direct nominations of {@code party} according to the polling
		 * station result file format.
		 *
		 * @param party the party to format the direct nominations for
		 * @return the formatted direct nominations
		 */
		private String formatDirectNominations(final Party party) {
			final LocalDistrict district = pollingStation.getDistrict();

			return formatNominations(result.getElection()
					.getNominations(party)
					.stream()
					.filter(nomination -> nomination.getDistrict().equals(district)
							&& nomination.getType() == LocalNominationType.DIRECT)
					.collect(toLinkedHashSet()));
		}

		/**
		 * Formats {@code nominations} according to the polling station result file
		 * format.
		 *
		 * @param nominations the set of nominations to format
		 * @return the formatted {@code nominations}
		 */
		private static String formatNominations(final Set<LocalNomination> nominations) {
			return nominations.stream()
					.map(LocalNomination::getPerson)
					.map(person -> person.getFamilyName() + ',' + person.getGivenName())
					.map(value -> Strings.replaceAll(value, SINGLE_SPACE_PATTERN, "_"))
					.collect(joining(" "));
		}

		/**
		 * Election Result to write
		 *
		 * @return the election result to write
		 */
		LocalElectionResult result;

		/**
		 * Polling Station to write
		 *
		 * @return the polling station to write
		 */
		LocalPollingStation pollingStation;

		/**
		 * Polling Station Result file writer
		 *
		 * @return the polling station result file writer
		 */
		Writer writer;

		/**
		 * Formats and writes the {@link #result} of {@link #pollingStation} to
		 * {@link #writer}.
		 *
		 * @throws IOException on IO error
		 */
		@SuppressWarnings("checkstyle:MultipleStringLiterals")
		public void write() throws IOException {
			write("%s %s\n", LINE_COMMENT, pollingStation.getName());

			final OptionalInt numberOfEligibleVoters = result.getElection().getNumberOfEligibleVoters();
			if (numberOfEligibleVoters.isPresent()) {
				write("%s Anzahl Wahlberechtigte: %d\n", LINE_COMMENT, numberOfEligibleVoters.getAsInt());
			}

			final OptionalInt numberOfAllBallots = result.getNumberOfAllBallots(pollingStation);
			if (numberOfAllBallots.isPresent()) {
				write("%s Anzahl Stimmzettel: %d\n", LINE_COMMAND, numberOfAllBallots.getAsInt());
			}
			write("\n");

			final int numberOfInvaliBallots = result.getNumberOfInvalidBallots();
			if (numberOfInvaliBallots > 0) {
				write("%s Ungültige Stimmzettel\n", LINE_COMMENT);
				write("%d %s\n\n", numberOfInvaliBallots, BALLOT_INVALID);
			}

			final Collection<LocalPartyResult> partyResults = result.getPartyResults().values();
			if (!partyResults.isEmpty()) {
				write("%s Blockstimmen\n", LINE_COMMENT);
				for (final LocalPartyResult partyResult : partyResults) {
					write("%d %s\n",
							partyResult.getNumberOfBlockVotings(),
							formatDirectNominations(partyResult.getParty()));
				}
				write("\n");
			}

			write("%s Stimmzettel\n", LINE_COMMENT);
			for (final LocalBallot ballot : result.getBallots()) {
				if (!ballot.isBlockVoting()) {
					write("%s\n", formatNominations(ballot.getNominations()));
				}
			}
		}

		/**
		 * Formats and writes the value {@code format} by applying {@code args}.
		 *
		 * @param format the format string
		 * @param args   the arguments referenced by format specifiers in {@code format}
		 * @throws IOException on IO error
		 */
		private void write(final String format, final Object... args) throws IOException {
			writer.write(Strings.format(format, args));
		}
	}
}
