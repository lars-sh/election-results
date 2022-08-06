package de.larssh.election.germany.schleswigholstein.local.cli;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.jar.Attributes.Name;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.local.LocalBallot;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.file.AwgWebsiteFiles;
import de.larssh.election.germany.schleswigholstein.local.file.PresentationFiles;
import de.larssh.utils.Nullables;
import de.larssh.utils.function.DoubleToDoubleFunction;
import de.larssh.utils.function.ThrowingConsumer;
import de.larssh.utils.io.Resources;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * The CLI interface for {@link LocalElectionResult}
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
@Command(name = "election-results",
		mixinStandardHelpOptions = true,
		versionProvider = LocalElectionResultCli.class,
		description = "These commands can be used to convert on eor more polling station results to e.g. human-readable file types.")
public class LocalElectionResultCli implements IVersionProvider {
	/**
	 * CLI description for {@code FILE} parameters
	 */
	private static final String DESCRIPTION_FILE
			= "File to write to.\nIn case the file exists already it is overwritten.";

	/**
	 * CLI description for {@code FILE} parameters, whose files are written atomic
	 */
	private static final String DESCRIPTION_FILE_ATOMIC
			= DESCRIPTION_FILE + "\nWriting is done atomic to avoid blank browser screens.";

	/**
	 * One hundred
	 */
	private static final int HUNDRED = 100;

	/**
	 * CLI parameter label for "either a number of ballots or a percentage"
	 */
	private static final String PARAM_LABEL_ABSOLUTE_BALLOTS_OR_PERCENTAGE = "<Number of Ballots>|<Percentage>";

	/**
	 * Width of the CLI help's usage messages
	 */
	private static final int USAGE_HELP_WIDTH = 160;

	/**
	 * The CLI interface for {@link LocalElectionResult}.
	 *
	 * @param args CLI arguments
	 */
	@SuppressWarnings("checkstyle:UncommentedMain")
	public static void main(final String... args) {
		System.exit(new CommandLine(new LocalElectionResultCli()).execute(args));
	}

	/**
	 * Current {@link CommandSpec} instance
	 */
	@Spec
	@NonFinal
	@Nullable
	CommandSpec commandSpec = null;

	/**
	 * Creates a PHP file to be used for the AWG web site.
	 *
	 * @param result the result
	 * @param output the path to write to
	 * @throws IOException on IO error
	 */
	@Command(name = "awg-website",
			showDefaultValues = true,
			usageHelpWidth = USAGE_HELP_WIDTH,
			description = "Creates a PHP file to be used for the AWG web site.")
	public void awgWebsite(@Mixin final LocalElectionResultParameter result,
			@Parameters(paramLabel = "FILE", description = DESCRIPTION_FILE) final Path output) throws IOException {
		try (Writer writer = Files.newBufferedWriter(output)) {
			AwgWebsiteFiles.write(result.read(), writer);
		}
	}

	/**
	 * Creates a HTML presentation format for the election result.
	 *
	 * @param result      the result
	 * @param output      the path to write to
	 * @param watch       if {@code true} the input files are watched for updates
	 *                    and the application does not return its handle
	 * @param refreshRate the refresh rate of the HTML file or empty
	 * @throws InterruptedException if interrupted while watching for file changes
	 * @throws IOException          on IO error
	 */
	@SuppressWarnings({ "checkstyle:SuppressWarnings", "resource" })
	@Command(showDefaultValues = true,
			usageHelpWidth = USAGE_HELP_WIDTH,
			description = "Creates a HTML presentation format for the election result.")
	public void presentation(@Mixin final LocalElectionResultParameter result,
			@Parameters(paramLabel = "FILE", description = DESCRIPTION_FILE_ATOMIC) final Path output,
			@Option(names = "--watch",
					defaultValue = "false",
					description = "Watches the input files for updates and does not return the application's handle") final boolean watch,
			@Option(names = "--refresh",
					defaultValue = "PT0S",
					paramLabel = "<Duration>",
					description = "Allows to specify a duration after which the HTML page refreshes automatically\nExample for one second: PT1S") final Duration refreshRate)
			throws InterruptedException, IOException {
		final ThrowingConsumer<LocalElectionResult> handler = readResult -> {
			writePresentationFile(Nullables.orElseThrow(readResult), Optional.of(refreshRate), output);
			getStandardOutputWriter().println(String.format("Updated at %1$tT %1$tZ", ZonedDateTime.now()));
		};

		// Execute
		handler.accept(result.read());
		if (watch) {
			result.watch(handler);
		}
	}

	/**
	 * Creates a HTML presentation format for multiple points \"in time\". This
	 * allows to reproduce/demo the counting process.
	 *
	 * @param result        the result
	 * @param output        the path to write to
	 * @param sleepDuration the duration to wait between the time-travel steps
	 * @param stepSize      size per step of the time-travel
	 * @param start         start of the time-travel
	 * @param end           end of the time-travel
	 * @param refreshEnd    do not stop automatic browser refreshing when reaching
	 *                      the end
	 * @param noRefresh     disables automatic browser refreshing
	 * @throws InterruptedException if interrupted while watching for file changes
	 * @throws IOException          on IO error
	 */
	@SuppressWarnings({
			"checkstyle:ParameterNumber",
			"checkstyle:SuppressWarnings",
			"PMD.AvoidInstantiatingObjectsInLoops",
			"resource" })
	@SuppressFBWarnings(value = "MDM_THREAD_YIELD",
			justification = "There is really nothin to do for the thread in the meantime.")
	@Command(name = "time-travel",
			showDefaultValues = true,
			usageHelpWidth = USAGE_HELP_WIDTH,
			description = "Creates a HTML presentation format for multiple points \"in time\".\nThis allows to reproduce/demo the counting process.")
	public void timeTravel(@Mixin final LocalElectionResultParameter result,
			@Parameters(paramLabel = "FILE", description = DESCRIPTION_FILE_ATOMIC) final Path output,
			@Option(names = "--sleep",
					defaultValue = "PT1S",
					paramLabel = "<Duration>",
					description = "Duration to wait between the time-travel steps") final Duration sleepDuration,
			@Option(names = "--step-size",
					defaultValue = "1",
					paramLabel = PARAM_LABEL_ABSOLUTE_BALLOTS_OR_PERCENTAGE,
					converter = EitherAbsoluteOrPercentageTypeConverter.class,
					description = "Size per step of the time-travel") final DoubleToDoubleFunction stepSize,
			@Option(names = "--start",
					defaultValue = "0",
					paramLabel = PARAM_LABEL_ABSOLUTE_BALLOTS_OR_PERCENTAGE,
					converter = EitherAbsoluteOrPercentageTypeConverter.class,
					description = "Start of the time-travel") final DoubleToDoubleFunction start,
			@Option(names = "--end",
					defaultValue = "100%",
					paramLabel = PARAM_LABEL_ABSOLUTE_BALLOTS_OR_PERCENTAGE,
					converter = EitherAbsoluteOrPercentageTypeConverter.class,
					description = "End of the time-travel") final DoubleToDoubleFunction end,
			@Option(names = "--refresh-end",
					defaultValue = "false",
					description = "Do not stop automatic browser refreshing when reaching the end") final boolean refreshEnd,
			@Option(names = "--no-refresh",
					defaultValue = "false",
					description = "Disables automatic browser refreshing") final boolean noRefresh)
			throws InterruptedException, IOException {
		// Read full result
		final LocalElectionResult fullResult = result.read();
		final Map<District<?>, OptionalInt> numberOfAllBallotsPerDistrict = fullResult.getElection()
				.getDistricts()
				.stream()
				.collect(toMap(identity(), fullResult::getNumberOfAllBallots));

		// Calculate the loop arguments
		// Double values are used to be as close to the percentage values as possible
		final int numberOfAllBallots = fullResult.getBallots().size();
		final double loopStart = start.applyAsDouble(numberOfAllBallots);
		final double loopEnd = Math.min(end.applyAsDouble(numberOfAllBallots), numberOfAllBallots);
		final double loopStepSize = stepSize.applyAsDouble(numberOfAllBallots);

		for (double loopPosition = loopStart; loopPosition < loopEnd + loopStepSize; loopPosition += loopStepSize) {
			// Calculate the correct discrete position and making sure it does not extend
			// the number of all ballots
			final int numberOfBallots = Math.min((int) loopPosition, (int) loopEnd);

			// Stop refreshing the page when reached the end
			final Optional<Duration> refreshRate = !noRefresh && (numberOfBallots < numberOfAllBallots || refreshEnd)
					? Optional.of(sleepDuration)
					: Optional.empty();

			// Create partial result and write the presentation file
			final LocalElectionResult partialResult = new LocalElectionResult(fullResult.getElection(),
					fullResult.getSainteLagueScale(),
					numberOfAllBallotsPerDistrict,
					fullResult.getDirectDrawResults(),
					fullResult.getListDrawResults(),
					fullResult.getBallots()
							.stream()
							.sorted(Comparator.comparing(LocalBallot::getPollingStation))
							.limit(numberOfBallots)
							.collect(toList()));
			writePresentationFile(partialResult, refreshRate, output);

			// Provide a user readable status and sleep
			getStandardOutputWriter().println(String.format(
					"Travelled to %"
							+ Integer.toString(numberOfAllBallots).length()
							+ "d of %d (%5.1f%%) at %4$tT %4$tZ",
					numberOfBallots,
					numberOfAllBallots,
					Math.min(loopPosition * HUNDRED / numberOfAllBallots, HUNDRED),
					ZonedDateTime.now()));
			Thread.sleep(sleepDuration.toMillis());
		}
	}

	/**
	 * Returns the standard output writer based on the current {@link CommandSpec}.
	 *
	 * @return the standard output writer
	 */
	private PrintWriter getStandardOutputWriter() {
		return Nullables.orElseThrow(commandSpec).commandLine().getOut();
	}

	/** {@inheritDoc} */
	@Override
	public String[] getVersion() throws IOException {
		return new String[] {
				Resources.readManifest(getClass())
						.map(manifest -> manifest.getMainAttributes().get(Name.IMPLEMENTATION_VERSION).toString())
						.orElse("unknown") };
	}

	/**
	 * Dummy to avoid the IDE to mark some fields as {@code final}.
	 */
	@SuppressWarnings({ "PMD.NullAssignment", "PMD.UnusedPrivateMethod" })
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "dummy method")
	private void nonFinalDummy() {
		commandSpec = null;
	}

	/**
	 * Writes {@code result} as presentation file to {@code output}. To avoid blank
	 * browser screens writing is done to a temporary file beneath {@code output}
	 * first. Either creating or overwriting {@code output} is done atomic
	 * afterwards.
	 *
	 * @param result      the result
	 * @param refreshRate the refresh rate of the HTML file or empty
	 * @param output      the path to write to
	 * @throws IOException on IO error
	 */
	@SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "\"output\" is really expected to be a user input")
	private void writePresentationFile(final LocalElectionResult result,
			final Optional<Duration> refreshRate,
			final Path output) throws IOException {
		final Path outputParentFolder = output.toAbsolutePath().getParent();
		if (outputParentFolder == null) {
			throw new IllegalArgumentException(
					String.format("Path \"%s\" seems to be no file, as there is no parent folder.", output));
		}

		// Create temporary file
		final Path tempFile = Files
				.createTempFile(outputParentFolder, Nullables.orElseThrow(output.getFileName()).toString(), ".tmp");
		tempFile.toFile().deleteOnExit();

		// Write temporary file
		try (Writer writer = Files.newBufferedWriter(tempFile)) {
			PresentationFiles.write(result, refreshRate, writer);
		}

		// Move atomic
		Files.move(tempFile, output, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
	}
}
