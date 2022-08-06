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
import java.util.Map;
import java.util.OptionalInt;
import java.util.jar.Attributes.Name;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.file.AwgWebsiteFiles;
import de.larssh.election.germany.schleswigholstein.local.file.PresentationFiles;
import de.larssh.utils.Nullables;
import de.larssh.utils.function.IntToIntFunction;
import de.larssh.utils.function.ThrowingConsumer;
import de.larssh.utils.io.Resources;
import edu.umd.cs.findbugs.annotations.Nullable;
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
 * The CLI interface for {@link LocalElectionResult}.
 */
@Command(name = "election-results",
		mixinStandardHelpOptions = true,
		versionProvider = LocalElectionResultCli.class,
		description = "These commands can be used to convert on eor more polling station results to e.g. human-readable file types.")
public class LocalElectionResultCli implements IVersionProvider {
	/**
	 * One hundred
	 */
	private static final int HUNDRED = 100;

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
	@Command(name = "awg-website", description = "Creates a PHP file to be used for the AWG web site.")
	public void awgWebsite(@Mixin final LocalElectionResultParameter result,
			@Parameters(paramLabel = "FILE", description = DESCRIPTION_FILE) final Path output) throws IOException {
		try (Writer writer = Files.newBufferedWriter(output)) {
			AwgWebsiteFiles.write(result.read(), writer);
		}
	}

	/**
	 * Creates a HTML presentation format for the election result.
	 *
	 * @param result the result
	 * @param output the path to write to
	 * @param watch  if {@code true} the input files are watched for updates and the
	 *               application does not return its handle
	 * @throws InterruptedException if interrupted while watching for file changes
	 * @throws IOException          on IO error
	 */
	@SuppressWarnings({ "checkstyle:SuppressWarnings", "resource" })
	@Command(description = "Creates a HTML presentation format for the election result.")
	public void presentation(@Mixin final LocalElectionResultParameter result,
			@Parameters(paramLabel = "FILE", description = DESCRIPTION_FILE_ATOMIC) final Path output,
			@Option(names = "--watch",
					defaultValue = "false",
					description = "Watches the input files for updates and does not return the application's handle") final boolean watch)
			throws InterruptedException, IOException {
		final ThrowingConsumer<LocalElectionResult> handler = readResult -> {
			writePresentationFile(Nullables.orElseThrow(readResult), output);
			getStandardOutputWriter().println(String.format("Updated at %1$tT %1$tZ", ZonedDateTime.now()));
		};

		// Execute
		handler.accept(result.read());
		if (watch) {
			result.watch(handler);
		}
	}

	@SuppressWarnings({ "checkstyle:SuppressWarnings", "resource" })
	@Command(name = "time-travel", description = "TODO")
	public void timeTravel(@Mixin final LocalElectionResultParameter resultParameter,
			@Parameters(paramLabel = "FILE", description = DESCRIPTION_FILE_ATOMIC) final Path output,
			@Option(names = "--sleep", defaultValue = "PT1S", description = "TODO") final Duration sleepDuration,
			@Option(names = "--step-size",
					defaultValue = "1",
					converter = EitherAbsoluteOrPercentageTypeConverter.class,
					description = "TODO") final IntToIntFunction stepSizeParameter,
			@Option(names = "--start",
					defaultValue = "0",
					converter = EitherAbsoluteOrPercentageTypeConverter.class,
					description = "TODO") final IntToIntFunction start,
			@Option(names = "--end",
					defaultValue = "100",
					converter = EitherAbsoluteOrPercentageTypeConverter.class,
					description = "TODO") final IntToIntFunction endParameter)
			throws InterruptedException, IOException {
		final LocalElectionResult result = resultParameter.read();
		final Map<District<?>, OptionalInt> numberOfAllBallots = result.getElection()
				.getDistricts()
				.stream()
				.collect(toMap(identity(), result::getNumberOfAllBallots));

		final int numberOfResultBallots = result.getBallots().size();
		final int stepSize = stepSizeParameter.applyAsInt(numberOfResultBallots);
		final int end = endParameter.applyAsInt(numberOfResultBallots) + stepSize;
		for (int limit = start.applyAsInt(numberOfResultBallots); limit < end; limit += stepSize) {
			final LocalElectionResult subResult = new LocalElectionResult(result.getElection(),
					result.getSainteLagueScale(),
					numberOfAllBallots,
					result.getDirectDrawResults(),
					result.getListDrawResults(),
					result.getBallots()
							.stream()
							.sorted((a, b) -> b.getPollingStation().compareTo(a.getPollingStation()))
							.limit(limit)
							.collect(toList()));

			writePresentationFile(subResult, output);
			getStandardOutputWriter().println(String.format("Travelled to %5.1f%% at %2$tT %2$tZ",
					Math.min(HUNDRED, (double) limit * HUNDRED / numberOfResultBallots),
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
	@SuppressWarnings("unused")
	private void nonFinalDummy() {
		commandSpec = null;
	}

	/**
	 * Writes {@code result} as presentation file to {@code output}. To avoid blank
	 * browser screens writing is done to a temporary file beneath {@code output}
	 * first. Either creating or overwriting {@code output} is done atomic
	 * afterwards.
	 *
	 * @param result the result
	 * @param output the path to write to
	 * @throws IOException on IO error
	 */
	private void writePresentationFile(final LocalElectionResult result, final Path output) throws IOException {
		final Path tempFile
				= Files.createTempFile(output.toAbsolutePath().getParent(), output.getFileName().toString(), ".tmp");
		tempFile.toFile().deleteOnExit();

		try (Writer writer = Files.newBufferedWriter(tempFile)) {
			PresentationFiles.write(result, writer);
		}
		Files.move(tempFile, output, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
	}
}
