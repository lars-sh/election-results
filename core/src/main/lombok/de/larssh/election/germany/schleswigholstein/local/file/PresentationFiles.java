package de.larssh.election.germany.schleswigholstein.local.file;

import static de.larssh.utils.Collectors.toLinkedHashSet;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;

import de.larssh.election.germany.schleswigholstein.Color;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalBallot;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationType;
import de.larssh.election.germany.schleswigholstein.local.LocalPartyResult;
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
 * This class contains helper methods to write a result file for live
 * presentations.
 */
@UtilityClass
@SuppressWarnings("PMD.ExcessiveImports")
public class PresentationFiles {
	/**
	 * Formats and writes {@code result} to {@code writer}.
	 *
	 * @param result      the election result to to write
	 * @param refreshRate the refresh rate of the HTML file or empty
	 * @param writer      the live presentation file writer
	 * @throws IOException on IO error
	 */
	public static void write(final LocalElectionResult result,
			final Optional<Duration> refreshRate,
			final Writer writer) throws IOException {
		new PresentationFileWriter(result, refreshRate, writer).write();
	}

	/**
	 * This class writes data of a {@link LocalElectionResult} to a live
	 * presentation file.
	 */
	@RequiredArgsConstructor
	@SuppressFBWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE",
			justification = "Creating system independent output by design")
	private static class PresentationFileWriter {
		/**
		 * The value 100, with a scale of 0.
		 */
		private static final BigDecimal HUNDRED = BigDecimal.TEN.multiply(BigDecimal.TEN);

		/**
		 * Template file body (lazily loaded)
		 */
		private static final Supplier<String> TEMPLATE
				= Finals.lazy(() -> loadResourceRelativeToClass("template.html"));

		/**
		 * Template file body for the nomination result view (lazily loaded)
		 */
		private static final Supplier<String> TEMPLATE_NOMINATION_RESULT
				= Finals.lazy(() -> loadResourceRelativeToClass("template-nominationResult.html"));

		/**
		 * Template file body for the party result view (lazily loaded)
		 */
		private static final Supplier<String> TEMPLATE_PARTY_RESULT
				= Finals.lazy(() -> loadResourceRelativeToClass("template-partyResult.html"));

		/**
		 * Template file body for the polling station view (lazily loaded)
		 */
		private static final Supplier<String> TEMPLATE_POLLING_STATION
				= Finals.lazy(() -> loadResourceRelativeToClass("template-pollingStation.html"));

		/**
		 * Time zone for time stamps in presentation files
		 */
		private static final ZoneId TIME_ZONE = ZoneId.of("Europe/Berlin");

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
			final Path path = Resources.getResourceRelativeTo(clazz, Paths.get(fileName)).get();

			try {
				return new String(Files.readAllBytes(path), Strings.DEFAULT_CHARSET);
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		/**
		 * Encodes {@code value} according to the XML standards.
		 *
		 * @param value the value to encode
		 * @return the encoded value
		 */
		private static String encodeXml(final String value) {
			return value.replace("&", "&amp;")
					.replace("<", "&lt;")
					.replace(">", "&gt;")
					.replace("\"", "&quot;")
					.replace("'", "&apos;")
					.replace("\r", "&#13;")
					.replace("\n", "&#10;");
		}

		/**
		 * Formats {@code value} according to {@link Locale#GERMAN} and using the
		 * precision of one decimal.
		 *
		 * @param value the value
		 * @return the formatted value
		 */
		private static String formatBigDecimal(final BigDecimal value) {
			return String.format(Locale.GERMAN, "%.1f", value);
		}

		/**
		 * Election Result to write
		 *
		 * @return the election result to write
		 */
		LocalElectionResult result;

		/**
		 * Refresh rate of the HTML file or empty if the page shall not refresh
		 * automatically
		 *
		 * @return the refresh rate of the HTML file or empty
		 */
		Optional<Duration> refreshRate;

		/**
		 * Live presentation file writer
		 *
		 * @return the live presentation file writer
		 */
		Writer writer;

		/**
		 * Formats and writes {@link #result} to {@link #writer}.
		 *
		 * @throws IOException on IO error
		 */
		@PackagePrivate
		@SuppressFBWarnings(value = "OI_OPTIONAL_ISSUES_USES_IMMEDIATE_EXECUTION",
				justification = "The value 0L is nothing that needs to be executed.")
		void write() throws IOException {
			writer.write(String.format(TEMPLATE.get(),
					refreshRate.map(Duration::toMillis).orElse(0L),
					result.getElection().getDate(),
					formatPollingStations(),
					formatNominationResults(),
					formatPartyResults(),
					LocalDateTime.now(TIME_ZONE)));
		}

		/**
		 * Formats the polling stations overview.
		 *
		 * @return the polling stations overview
		 */
		private String formatPollingStations() {
			final Set<LocalPollingStation> pollingStations = result.getElection()
					.getDistricts()
					.stream()
					.filter(LocalPollingStation.class::isInstance)
					.map(LocalPollingStation.class::cast)
					.collect(toLinkedHashSet());
			final BigDecimal evaluationProgressIfUnknown
					= HUNDRED.divide(BigDecimal.valueOf(pollingStations.size()), 1, RoundingMode.HALF_UP);

			return pollingStations.stream()
					.map(pollingStation -> formatPollingStation(pollingStation,
							estimateNumberOfAllBallots(pollingStations),
							evaluationProgressIfUnknown))
					.collect(joining());
		}

		/**
		 * Returns the exact number of all ballots if available. ELse the number of all
		 * ballots is estimated.
		 *
		 * @param pollingStations the polling stations
		 * @return the estimated number of all ballots
		 */
		@SuppressWarnings("checkstyle:MagicNumber")
		private BigDecimal estimateNumberOfAllBallots(final Set<LocalPollingStation> pollingStations) {
			// Start by calculating the known numbers of ballots
			int numberOfPresent = 0;
			int sumOfAllBallots = 0;
			for (final LocalPollingStation pollingStation : pollingStations) {
				final OptionalInt numberOfAllBallots = result.getNumberOfAllBallots(pollingStation);
				if (numberOfAllBallots.isPresent()) {
					sumOfAllBallots += numberOfAllBallots.getAsInt();
					numberOfPresent += 1;
				}
			}

			// In case no information about the number of ballots is available, let's
			// estimate exactly one ballot per polling station.
			if (numberOfPresent == 0) {
				return BigDecimal.valueOf(pollingStations.size() - numberOfPresent);
			}

			// Increase the sum of known ballots proportionally
			return BigDecimal.valueOf(sumOfAllBallots)
					.multiply(BigDecimal.valueOf(pollingStations.size()))
					.divide(BigDecimal.valueOf(numberOfPresent), 3, RoundingMode.HALF_UP);
		}

		/**
		 * Formats the given {@code pollingStation}.
		 *
		 * @param pollingStation              the polling station to format
		 * @param estimatedNumberOfAllBallots the estimated number of all ballots
		 * @param evaluationProgressIfUnknown the evaluation progress in case the number
		 *                                    of all ballots is unknown
		 * @return the formatted polling station
		 */
		private String formatPollingStation(final LocalPollingStation pollingStation,
				final BigDecimal estimatedNumberOfAllBallots,
				final BigDecimal evaluationProgressIfUnknown) {
			final BigDecimal evaluationProgress
					= result.getEvaluationProgress(1, pollingStation).orElse(BigDecimal.ZERO);

			return String.format(Locale.ROOT,
					TEMPLATE_POLLING_STATION.get(),
					pollingStation.getBackgroundColor().toHex(),
					pollingStation.getFontColor().toHex(),
					OptionalInts
							.mapToObj(result.getNumberOfAllBallots(pollingStation),
									numOfBallots -> estimatedNumberOfAllBallots.compareTo(BigDecimal.ZERO) == 0
											? null
											: BigDecimal.valueOf(numOfBallots)
													.multiply(HUNDRED)
													.divide(estimatedNumberOfAllBallots, 1, RoundingMode.HALF_UP))
							.orElse(evaluationProgressIfUnknown),
					encodeXml(formatPollingStationTitle(pollingStation)),
					encodeXml(pollingStation.getName()),
					formatBigDecimal(evaluationProgress),
					evaluationProgress);
		}

		/**
		 * Formats the {@code title} attribute for the given {@code pollingStation}.
		 *
		 * @param pollingStation the polling station to format
		 * @return the formatted polling station {@code title} attribute
		 */
		private String formatPollingStationTitle(final LocalPollingStation pollingStation) {
			return formatPollingStationTitlePart(result.getBallots(pollingStation).size(),
					result.getNumberOfAllBallots(pollingStation),
					(int) result.getBallots(pollingStation).stream().filter(ballot -> !ballot.isValid()).count(),
					result.getElection().getNumberOfEligibleVoters(pollingStation))

					+ String.format("\n\nGesamt: %s\u202f%%\n",
							formatBigDecimal(result.getEvaluationProgress(1).orElse(BigDecimal.ZERO)))

					+ formatPollingStationTitlePart(result.getBallots().size(),
							result.getNumberOfAllBallots(),
							result.getNumberOfInvalidBallots(),
							result.getElection().getNumberOfEligibleVoters());
		}

		/**
		 * Supports formatting the {@code title} attribute by formatting one section
		 * (part).
		 *
		 * @param numberOfEvaluatedBallots the number of already evaluated ballots
		 * @param numberOfAllBallots       the number of all ballots
		 * @param numberOfInvalidBallots   the number of already evaluated invalid
		 *                                 ballots
		 * @param numberOfEligibleVoters   the number of eligible voters
		 * @return the formatted title part
		 */
		private String formatPollingStationTitlePart(final int numberOfEvaluatedBallots,
				final OptionalInt numberOfAllBallots,
				final int numberOfInvalidBallots,
				final OptionalInt numberOfEligibleVoters) {
			final StringBuilder builder = new StringBuilder();
			if (numberOfAllBallots.isPresent()) {
				builder.append(String.format("%d Stimmzettel\ndavon %d ausgezählt\ndavon %d ungültig",
						numberOfAllBallots.getAsInt(),
						numberOfEvaluatedBallots,
						numberOfInvalidBallots));
			} else {
				builder.append(String.format("%d Stimmzettel ausgezählt\ndavon %d ungültig",
						numberOfEvaluatedBallots,
						numberOfInvalidBallots));
			}

			if (numberOfEligibleVoters.isPresent()) {
				builder.append(String.format("\n\n%d Stimmberechtigte", numberOfEligibleVoters.getAsInt()));
				if (numberOfAllBallots.isPresent()) {
					builder.append(String.format("\nWahlbeteiligung: %s\u202f%%",
							formatBigDecimal(BigDecimal.valueOf(numberOfAllBallots.getAsInt())
									.multiply(HUNDRED)
									.divide(BigDecimal.valueOf(numberOfEligibleVoters.getAsInt()),
											1,
											RoundingMode.HALF_UP))));
				}
			}
			return builder.toString();
		}

		/**
		 * Formats the nomination results overview.
		 *
		 * @return the nomination results overview
		 */
		private String formatNominationResults() {
			final BigDecimal maxNumberOfVotes = result.getNominationResults()
					.values()
					.stream()
					.map(LocalNominationResult::getNumberOfVotes)
					.max(Integer::compare)
					.map(BigDecimal::valueOf)
					.orElse(BigDecimal.ZERO);

			return result.getNominationResults()
					.values()
					.stream()
					.filter(nominationResult -> nominationResult.getNomination().getType() == LocalNominationType.DIRECT
							|| nominationResult.getType() != LocalNominationResultType.NOT_ELECTED)
					.map(nominationResult -> formatNominationResult(nominationResult, maxNumberOfVotes))
					.collect(joining());
		}

		/**
		 * Formats the given nomination {@code result}.
		 *
		 * @param result           the nomination result to format
		 * @param maxNumberOfVotes the max number of votes of all nomination results
		 * @return the formatted nomination result
		 */
		private String formatNominationResult(final LocalNominationResult result, final BigDecimal maxNumberOfVotes) {
			return String.format(Locale.ROOT,
					TEMPLATE_NOMINATION_RESULT.get(),
					Strings.toLowerCaseAscii(result.getType().toString()),
					result.getCertainResultType()
							.map(certain -> "certain-" + Strings.toLowerCaseAscii(certain.toString()))
							.orElse("uncertain"),
					encodeXml(formatNominationResultTitle(result)),
					encodeXml(result.getNomination().getPerson().getKey()),
					result.getNumberOfVotes(),
					result.getNomination().getParty().map(Party::getBackgroundColor).orElse(Color.BLACK).toHex(),
					result.getNomination().getParty().map(Party::getFontColor).orElse(Color.WHITE).toHex(),
					maxNumberOfVotes.compareTo(BigDecimal.ZERO) == 0
							? BigDecimal.ZERO
							: BigDecimal.valueOf(result.getNumberOfVotes())
									.multiply(HUNDRED)
									.divide(maxNumberOfVotes, 1, RoundingMode.HALF_UP));
		}

		/**
		 * Formats the {@code title} attribute for the given nomination {@code result}.
		 *
		 * @param result the nomination result to format
		 * @return the formatted nomination result {@code title} attribute
		 */
		@SuppressWarnings("checkstyle:MultipleStringLiterals")
		private String formatNominationResultTitle(final LocalNominationResult result) {
			final int numberOfBallots = this.result.getBallots(result.getNomination().getDistrict()).size();
			final StringBuilder builder = new StringBuilder(String.format("Anteil: %s\u202f%%",
					formatBigDecimal(numberOfBallots == 0
							? BigDecimal.ZERO
							: BigDecimal.valueOf(result.getNumberOfVotes())
									.multiply(HUNDRED)
									.divide(BigDecimal.valueOf(numberOfBallots), 1, RoundingMode.HALF_UP))));

			result.getNomination()
					.getParty()
					.map(party -> String.format("\nListenposition %d der %s",
							new ArrayList<>(this.result.getElection().getNominations(party)) //
									.indexOf(result.getNomination())
									+ 1,
							party.getShortName()))
					.ifPresent(builder::append);
			result.getSainteLagueValue()
					.map(sainteLagueValue -> String.format(Locale.GERMAN,
							"\nSainte-Laguë-Höchstzahl: %." + sainteLagueValue.scale() + "f",
							sainteLagueValue))
					.ifPresent(builder::append);

			for (final LocalPollingStation pollingStation : result.getNomination().getDistrict().getChildren()) {
				final long numberOfVotesInPollingStation = result.getBallots()
						.stream()
						.filter(ballot -> ballot.getPollingStation().equals(pollingStation))
						.count();
				final long numberOfBallotsInPollingStation = this.result.getBallots(pollingStation).size();

				builder.append(String.format("\n\n%s: %s\u202f%%\nStimmen: %d",
						pollingStation.getName(),
						formatBigDecimal(numberOfBallotsInPollingStation == 0
								? BigDecimal.ZERO
								: BigDecimal.valueOf(numberOfVotesInPollingStation)
										.multiply(HUNDRED)
										.divide(BigDecimal.valueOf(numberOfBallotsInPollingStation),
												1,
												RoundingMode.HALF_UP)),
						numberOfVotesInPollingStation));
			}

			return builder.toString();
		}

		/**
		 * Formats the party results overview based on all party results and a section
		 * of the elected non-party nominations.
		 *
		 * @return the party results overview
		 */
		private String formatPartyResults() {
			final BigDecimal numberOfAllVotes = BigDecimal.valueOf(this.result.getBallots()
					.stream()
					.filter(LocalBallot::isValid)
					.map(LocalBallot::getNominations)
					.mapToInt(Set::size)
					.sum());
			final String formattedParties = result.getPartyResults()
					.values()
					.stream()
					.map(partyResult -> formatPartyResult(partyResult, numberOfAllVotes))
					.collect(joining());

			final Set<LocalNominationResult> nominationResultsWithoutParty = result.getNominationResults()
					.values()
					.stream()
					.filter(nominationResult -> !nominationResult.getNomination().getParty().isPresent())
					.collect(toLinkedHashSet());
			if (nominationResultsWithoutParty.isEmpty()) {
				return formattedParties;
			}

			return formattedParties
					+ formatPartyResult(Color.BLACK,
							Color.WHITE,
							"",
							"parteilos",
							(int) nominationResultsWithoutParty.stream()
									.filter(nominationResult -> nominationResult.getType().isElected())
									.count(),
							nominationResultsWithoutParty.stream()
									.mapToInt(LocalNominationResult::getNumberOfVotes)
									.sum(),
							numberOfAllVotes,
							nominationResultsWithoutParty);
		}

		/**
		 * Formats the given party {@code result}.
		 *
		 * @param result           the party result to format
		 * @param numberOfAllVotes the sum of votes of all valid ballots
		 * @return the formatted party result
		 */
		private String formatPartyResult(final LocalPartyResult result, final BigDecimal numberOfAllVotes) {
			return formatPartyResult(result.getParty().getBackgroundColor(),
					result.getParty().getFontColor(),
					formatPartyResultTitle(result),
					result.getParty().getShortName(),
					result.getNumberOfCertainSeats(),
					result.getNumberOfVotes(),
					numberOfAllVotes,
					result.getNominationResults().values());
		}

		/**
		 * Formats the given data as party result.
		 *
		 * @param backgroundColor   the background color
		 * @param fontColor         the font color
		 * @param title             the content of the title attribute
		 * @param name              the name to display
		 * @param numberOfSeats     the number of seats of this party
		 * @param numberOfVotes     the number of votes of this party
		 * @param numberOfAllVotes  the sum of votes of all valid ballots
		 * @param nominationResults the nomination results of this party
		 * @return the formatted party result
		 */
		@SuppressWarnings("checkstyle:ParameterNumber")
		private String formatPartyResult(final Color backgroundColor,
				final Color fontColor,
				final String title,
				final String name,
				final int numberOfSeats,
				final int numberOfVotes,
				final BigDecimal numberOfAllVotes,
				final Collection<LocalNominationResult> nominationResults) {
			return String.format(Locale.ROOT,
					TEMPLATE_PARTY_RESULT.get(),
					backgroundColor.toHex(),
					fontColor.toHex(),
					encodeXml(title),
					encodeXml(name),
					numberOfSeats,
					formatBigDecimal(numberOfAllVotes.compareTo(BigDecimal.ZERO) == 0
							? BigDecimal.ZERO
							: BigDecimal.valueOf(numberOfVotes)
									.multiply(HUNDRED)
									.divide(numberOfAllVotes, 1, RoundingMode.HALF_UP)),
					nominationResults.stream()
							.filter(nominationResult -> nominationResult.getType().isElected())
							.map(nominationResult -> "<li>"
									+ encodeXml(nominationResult.getNomination().getPerson().getKey())
									+ "</li>")
							.collect(joining()));
		}

		/**
		 * Formats the {@code title} attribute for the given party {@code result}.
		 *
		 * @param result the party result to format
		 * @return the formatted party result {@code title} attribute
		 */
		@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "PMD.InsufficientStringBufferDeclaration" })
		private String formatPartyResultTitle(final LocalPartyResult result) {
			final StringBuilder builder = new StringBuilder(String.format("%s\n%d Stimmen\ndavon %d Blockstimmen",
					result.getParty().getName(),
					result.getNumberOfVotes(),
					result.getNumberOfBlockVotings()
							* Math.min(this.result.getElection().getNumberOfDirectSeatsPerLocalDistrict(),
									this.result.getElection().getNominations(result.getParty()).size())));

			final Set<LocalPollingStation> pollingStations = this.result.getElection()
					.getDistrict()
					.getChildren()
					.stream()
					.map(LocalDistrict::getChildren)
					.flatMap(Set::stream)
					.collect(toLinkedHashSet());
			for (final LocalPollingStation pollingStation : pollingStations) {
				final long numberOfPartyVotesInPollingStation = result.getBallots()
						.stream()
						.filter(ballot -> ballot.getPollingStation().equals(pollingStation))
						.mapToLong(ballot -> ballot.getNominations()
								.stream()
								.filter(nomination -> nomination.getParty()
										.filter(result.getParty()::equals)
										.isPresent())
								.count())
						.sum();
				final int numberOfVotesInPollingStation = this.result.getNumberOfVotes(pollingStation);

				builder.append(String.format("\n\n%s: %s\u202f%%\nStimmen: %d",
						pollingStation.getName(),
						formatBigDecimal(numberOfVotesInPollingStation == 0
								? BigDecimal.ZERO
								: BigDecimal.valueOf(numberOfPartyVotesInPollingStation)
										.multiply(HUNDRED)
										.divide(BigDecimal.valueOf(numberOfVotesInPollingStation),
												1,
												RoundingMode.HALF_UP)),
						numberOfPartyVotesInPollingStation));
			}

			return builder.toString();
		}
	}
}
