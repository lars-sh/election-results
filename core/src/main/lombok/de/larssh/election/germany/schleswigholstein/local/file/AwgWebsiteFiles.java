package de.larssh.election.germany.schleswigholstein.local.file;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationType;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.Finals;
import de.larssh.utils.OptionalInts;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.io.Resources;
import de.larssh.utils.text.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods to write a result file for the AWG
 * website.
 */
@UtilityClass
public class AwgWebsiteFiles {
	/**
	 * Formats and writes {@code result} to {@code writer}.
	 *
	 * @param result the election result to to write
	 * @param writer the AWG website file writer
	 * @throws IOException on IO error
	 */
	public static void write(final LocalElectionResult result, final Writer writer) throws IOException {
		new AwgWebsiteFileWriter(result, writer).write();
	}

	/**
	 * This class writes data of a {@link LocalElectionResult} to a AWG website
	 * file.
	 */
	@RequiredArgsConstructor
	private static class AwgWebsiteFileWriter {
		/**
		 * Delimiter between PHP array entries
		 */
		private static final String PHP_ARRAY_ENTRIES_DELIMITER = ",\n";

		/**
		 * Prefix to start PHP array entries with
		 */
		@SuppressWarnings("checkstyle:MultipleStringLiterals")
		private static final String PHP_ARRAY_ENTRIES_PREFIX = "\n";

		/**
		 * Suffix to end PHP array entries with
		 */
		private static final String PHP_ARRAY_ENTRIES_SUFFIX = "\n\t";

		/**
		 * Pattern matching sequences to convert to replace with a dash when stripping
		 * down a value to a PHP compatible identifier.
		 */
		private static final Pattern PHP_IDENTIFIER_MAKE_DASH = Pattern.compile("[^a-z0-9]+");

		/**
		 * Pattern matching sequences to remove when stripping down a value to a PHP
		 * compatible identifier.
		 */
		private static final Pattern PHP_IDENTIFIER_REMOVE = Pattern.compile("(^[-0-9]+)|(-+$)");

		/**
		 * PHP reserved literal for {@code null}
		 */
		private static final String PHP_NULL = "null";

		/**
		 * Character to start and end a PHP string literal with
		 */
		private static final String PHP_STRING_LITERAL = "'";

		/**
		 * Template file body (lazily loaded)
		 */
		private static final Supplier<String> TEMPLATE = Finals.lazy(() -> loadResourceRelativeToClass("template.php"));

		/**
		 * Template file body for a person object (lazily loaded)
		 */
		private static final Supplier<String> TEMPLATE_PERSONS
				= Finals.lazy(() -> loadResourceRelativeToClass("template-person.php"));

		/**
		 * Creates a PHP string literal with a stripped-down {@code value} to be used
		 * for identifiers.
		 *
		 * @param value the value to strip down
		 * @return a PHP string literal with a stripped-down {@code value} to be used
		 *         for identifiers
		 */
		@SuppressWarnings("checkstyle:MultipleStringLiterals")
		private static String createPhpIdentifier(final String value) {
			String identifier = Strings.toLowerCaseNeutral(value)
					.replace("ä", "ae")
					.replace("ö", "oe")
					.replace("ü", "ue")
					.replace("ß", "ss");
			identifier = Strings.replaceAll(identifier, PHP_IDENTIFIER_MAKE_DASH, "-");
			identifier = Strings.replaceAll(identifier, PHP_IDENTIFIER_REMOVE, "");

			return PHP_STRING_LITERAL + identifier + PHP_STRING_LITERAL;
		}

		/**
		 * Creates a PHP string literal representing {@code value}.
		 *
		 * @param value the value to represent
		 * @return a PHP string literal representing {@code value}
		 */
		private static String createPhpString(final String value) {
			return PHP_STRING_LITERAL
					+ value.replace("\r", "\\r")
							.replace("\n", "\\n")
							.replace("\\", "\\\\")
							.replace(PHP_STRING_LITERAL, "\\'")
					+ PHP_STRING_LITERAL;
		}

		/**
		 * Loads a resource from a folder next to this class. The file name is build by
		 * concatenating the class name, a dash and {@code fileNameSuffix}.
		 *
		 * @param fileNameSuffix the file name's suffix
		 * @return the resource file content
		 */
		@SuppressFBWarnings(value = { "EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS", "PATH_TRAVERSAL_IN" },
				justification = "IOExceptions in here are not expected to be related to user input or behavior "
						+ "and fileNameSuffix is expected to be a constant value within this class.")
		private static String loadResourceRelativeToClass(final String fileNameSuffix) {
			final Class<?> clazz = MethodHandles.lookup().lookupClass();
			final String fileName = clazz.getSimpleName() + "-" + fileNameSuffix;
			final Path path = Resources.getResourceRelativeTo(clazz, Paths.get(fileName))
					.orElseThrow(() -> new RuntimeException(
							String.format("Could not load \"%s\" from resources.", fileName)));

			try {
				return new String(Files.readAllBytes(path), Strings.DEFAULT_CHARSET);
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		/**
		 * Election Result to write
		 *
		 * @return the election result to write
		 */
		LocalElectionResult result;

		/**
		 * AWG website file writer
		 *
		 * @return the AWG website file writer
		 */
		Writer writer;

		/**
		 * Formats and writes {@link #result} to {@link #writer}.
		 *
		 * @throws IOException on IO error
		 */
		@PackagePrivate
		void write() throws IOException {
			writer.write(String.format(TEMPLATE.get(),
					result.getElection().getDate(),
					OptionalInts.mapToObj(result.getElection().getNumberOfEligibleVoters(), Integer::toString)
							.orElse(PHP_NULL),
					OptionalInts.mapToObj(result.getNumberOfAllBallots(), Integer::toString).orElse(PHP_NULL),
					result.getNumberOfPostalBallots(),
					result.getNumberOfInvalidBallots(),
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
					.getPollingStations()
					.stream()
					.map(pollingStation -> String.format("\t\t%s => array(%s)",
							createPhpIdentifier(pollingStation.getName()),
							formatNominationVotes(pollingStation)))
					.collect(joining(PHP_ARRAY_ENTRIES_DELIMITER, PHP_ARRAY_ENTRIES_PREFIX, PHP_ARRAY_ENTRIES_SUFFIX));
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
			return result.filterByDistrict(pollingStation)
					.getNominationResults()
					.values()
					.stream()
					.filter(nominationResult -> nominationResult.getNomination()
							.getType() == LocalNominationType.DIRECT)
					.map(nominationResult -> String.format("\t\t\t%s => %d",
							createPhpIdentifier(nominationResult.getNomination().getPerson().getKey()),
							nominationResult.getNumberOfVotes()))
					.collect(joining(PHP_ARRAY_ENTRIES_DELIMITER,
							PHP_ARRAY_ENTRIES_PREFIX,
							PHP_ARRAY_ENTRIES_SUFFIX + '\t'));
		}

		/**
		 * Returns PHP array entries by nomination for the "persons" array. An
		 * identifier is generated out of the nomination's key, referencing an object
		 * with its party, family name and given name.
		 *
		 * @return the PHP array entries
		 */
		private String formatPersons() {
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
					.collect(joining(PHP_ARRAY_ENTRIES_DELIMITER, PHP_ARRAY_ENTRIES_PREFIX, PHP_ARRAY_ENTRIES_SUFFIX));
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
					.getPollingStations()
					.stream()
					.map(pollingStation -> String.format("\t\t%s => %s",
							createPhpIdentifier(pollingStation.getName()),
							createPhpString(pollingStation.getName())))
					.collect(joining(PHP_ARRAY_ENTRIES_DELIMITER, PHP_ARRAY_ENTRIES_DELIMITER, ""));
		}
	}
}
