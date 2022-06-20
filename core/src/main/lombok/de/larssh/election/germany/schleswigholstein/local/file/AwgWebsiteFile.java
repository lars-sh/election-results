package de.larssh.election.germany.schleswigholstein.local.file;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalBallot;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationType;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.Finals;
import de.larssh.utils.OptionalInts;
import de.larssh.utils.io.Resources;
import de.larssh.utils.text.Strings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods to write a result file for the AWG
 * website.
 */
@UtilityClass
public class AwgWebsiteFile {
	public static void write(final LocalElectionResult result, final Writer writer) throws IOException {
		new AwgWebsiteFileWriter(result, writer).write();
	}

	@RequiredArgsConstructor
	private static class AwgWebsiteFileWriter {
		private static final String PHP_ARRAY_ENTRIES_DELIMITER = ",\n";

		@SuppressWarnings("checkstyle:MultipleStringLiterals")
		private static final String PHP_ARRAY_ENTRIES_PREFIX = "\n";

		private static final String PHP_ARRAY_ENTRIES_SUFFIX = "\n\t";

		private static final Pattern PHP_IDENTIFIER_MAKE_DASH = Pattern.compile("[^a-z0-9]+");

		private static final Pattern PHP_IDENTIFIER_REMOVE = Pattern.compile("(?:^[-0-9]+)|(?:-+$)");

		private static final String PHP_NULL = "null";

		private static final String PHP_STRING_LITERAL = "'";

		private static final Supplier<String> TEMPLATE = Finals.lazy(() -> loadResourceRelativeToClass("template.php"));

		private static final Supplier<String> TEMPLATE_PERSONS
				= Finals.lazy(() -> loadResourceRelativeToClass("template-person.php"));

		private static String createPhpIdentifier(final String value) {
			String identifier = Strings.toLowerCaseNeutral(value)
					.replace("ä", "ae")
					.replace("ö", "oe")
					.replace("ü", "ue")
					.replace("ß", "ss");
			identifier = Strings.replaceAll(identifier, PHP_IDENTIFIER_MAKE_DASH, "");
			identifier = Strings.replaceAll(identifier, PHP_IDENTIFIER_REMOVE, "");

			return PHP_STRING_LITERAL + identifier + PHP_STRING_LITERAL;
		}

		private static String createPhpString(final String value) {
			return PHP_STRING_LITERAL
					+ value.replace("\r", "\\r")
							.replace("\n", "\\n")
							.replace("\\", "\\\\")
							.replace(PHP_STRING_LITERAL, "\\'")
					+ PHP_STRING_LITERAL;
		}

		private static String loadResourceRelativeToClass(final String fileNameSuffix) {
			final Class<?> clazz = MethodHandles.lookup().lookupClass();
			final String fileName = clazz.getSimpleName() + "-" + fileNameSuffix;
			final Path path = Resources.getResourceRelativeTo(clazz, Paths.get(fileName)).get();

			try {
				return new String(Files.readAllBytes(path), Strings.DEFAULT_CHARSET);
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		LocalElectionResult result;

		Writer writer;

		private void write() throws IOException {
			final String wahlberechtigteOrNull
					= OptionalInts.mapToObj(result.getElection().getNumberOfEligibleVoters(), Integer::toString)
							.orElse(PHP_NULL);
			final String stimmzettelOrNull
					= OptionalInts.mapToObj(result.getNumberOfAllBallots(), Integer::toString).orElse(PHP_NULL);
			final long davonBriefwaehler = result.getBallots().stream().filter(LocalBallot::isPostalVote).count();
			final int ungueltigeStimmen = result.getNumberOfInvalidBallots();

			writer.write(String.format(TEMPLATE.get(),
					result.getElection().getDate(),
					wahlberechtigteOrNull,
					stimmzettelOrNull,
					davonBriefwaehler,
					ungueltigeStimmen,
					formatData(),
					formatPersons(),
					formatSeats(),
					formatTypes()));
		}

		/**
		 * Returns PHP array entries by polling station for the "data" array. An
		 * identifier is generated out of the polling station's name, referencing an
		 * object with its direct nominations and their number of votes.
		 *
		 * @return the PHP array entries
		 */
		private String formatData() {
			return result.getElection()
					.getDistrict()
					.getChildren()
					.stream()
					.map(LocalDistrict::getChildren)
					.flatMap(Set::stream)
					.map(pollingStation -> String.format("\t\t%s => array(%s)",
							createPhpIdentifier(pollingStation.getName()),
							formatNominationVotes(pollingStation)))
					.collect(Collectors
							.joining(PHP_ARRAY_ENTRIES_DELIMITER, PHP_ARRAY_ENTRIES_PREFIX, PHP_ARRAY_ENTRIES_SUFFIX));
		}

		/**
		 * Returns PHP array entries by the nominations of {@code pollingStation} for
		 * the objects inside the "data" array. An identifier is generated out of the
		 * nomination's key, referencing its number of votes.
		 *
		 * @param pollingStation the polling station
		 * @return the PHP array entries
		 */
		private String formatNominationVotes(final LocalPollingStation pollingStation) {
			return result.filter(ballot -> ballot.getPollingStation().equals(pollingStation))
					.getNominationResults()
					.values()
					.stream()
					.filter(nominationResult -> nominationResult.getNomination()
							.getType() == LocalNominationType.DIRECT)
					.map(nominationResult -> String.format("\t\t\t%s => %d",
							createPhpIdentifier(nominationResult.getNomination().getPerson().getKey()),
							nominationResult.getNumberOfVotes()))
					.collect(Collectors.joining(PHP_ARRAY_ENTRIES_DELIMITER,
							PHP_ARRAY_ENTRIES_PREFIX,
							PHP_ARRAY_ENTRIES_SUFFIX + '\t'));
		}

		/**
		 * Returns PHP array entries by nomination for the "persons" array. An
		 * identifier is generated out of the nomination's key, referencing an object
		 * with its party, family name and given name.
		 *
		 * @return the PHP array entries
		 * @throws IOException on IO error, reading the template file
		 */
		private String formatPersons() throws IOException {
			return result.getElection()
					.getNominations()
					.stream()
					.map(nomination -> String.format(TEMPLATE_PERSONS.get(),
							createPhpIdentifier(nomination.getPerson().getKey()),
							nomination.getParty()
									.map(Party::getShortName)
									.map(AwgWebsiteFileWriter::createPhpIdentifier)
									.orElse(PHP_NULL),
							createPhpString(nomination.getPerson().getFamilyName()),
							createPhpString(nomination.getPerson().getGivenName())))
					.collect(joining(PHP_ARRAY_ENTRIES_DELIMITER, PHP_ARRAY_ENTRIES_PREFIX, PHP_ARRAY_ENTRIES_SUFFIX));
		}

		/**
		 * Returns PHP array entries by party for the "seats" array. An identifier is
		 * generated out of the party's short name, referencing its number of seats.
		 *
		 * @return the PHP array entries
		 */
		private String formatSeats() {
			return result.getPartyResults()
					.values()
					.stream()
					.map(partyResult -> String.format("\t\t%s => %d",
							createPhpIdentifier(partyResult.getParty().getShortName()),
							partyResult.getNumberOfSeats()))
					.collect(Collectors
							.joining(PHP_ARRAY_ENTRIES_DELIMITER, PHP_ARRAY_ENTRIES_PREFIX, PHP_ARRAY_ENTRIES_SUFFIX));
		}

		/**
		 * Returns PHP array entries by polling station for the "types" array. An
		 * identifier is generated out of the polling station's name, referencing its
		 * name.
		 *
		 * @return the PHP array entries
		 */
		private String formatTypes() {
			return result.getElection()
					.getDistrict()
					.getChildren()
					.stream()
					.map(LocalDistrict::getChildren)
					.flatMap(Set::stream)
					.map(pollingStation -> String.format("\t\t%s => %s",
							createPhpIdentifier(pollingStation.getName()),
							createPhpString(pollingStation.getName())))
					.collect(Collectors.joining(PHP_ARRAY_ENTRIES_DELIMITER, PHP_ARRAY_ENTRIES_DELIMITER, ""));
		}
	}
}
