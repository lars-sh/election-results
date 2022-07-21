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
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.OptionalInt;
import java.util.jar.Attributes.Name;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.file.AwgWebsiteFiles;
import de.larssh.election.germany.schleswigholstein.local.file.PresentationFiles;
import de.larssh.utils.Nullables;
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

@Command(name = "election-results",
		mixinStandardHelpOptions = true,
		versionProvider = LocalElectionResultCli.class,
		description = "TODO")
public class LocalElectionResultCli implements IVersionProvider {
	/**
	 * One hundred
	 */
	private static final int HUNDRED = 100;

	private static final String DESCRIPTION_FILE_WRITE = "File to write to.";

	private static final String DESCRIPTION_OVERWRITE_FILE = "\nIn case the file exists already it is overwritten.";

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

	@Command(name = "awg-website", description = "Creates a PHP file to be used for the AWG website.")
	public void awgWebsite(@Mixin final LocalElectionResultParameter result,
			@Parameters(paramLabel = "FILE",
					description = DESCRIPTION_FILE_WRITE + DESCRIPTION_OVERWRITE_FILE) final Path output)
			throws IOException {
		try (Writer writer = Files.newBufferedWriter(output)) {
			AwgWebsiteFiles.write(result.read(), writer);
		}
	}

	@SuppressWarnings("resource")
	@Command(description = "Creates a HTML presentation format for the election result.")
	public void presentation(@Mixin final LocalElectionResultParameter result,
			@Parameters(paramLabel = "FILE",
					description = DESCRIPTION_FILE_WRITE
							+ "\nWriting is done atomic to avoid blank browser screens."
							+ DESCRIPTION_OVERWRITE_FILE) final Path output,
			@Option(names = "--loop", defaultValue = "false", description = "TODO") final boolean loop)
			throws IOException, InterruptedException {
		result.handle(loop, readResult -> {
			writePresentationFile(readResult, output);
			getStandardOutputWriter().println(String.format("Updated at %1$tT %1$tZ", ZonedDateTime.now()));
		});
	}

	@SuppressWarnings("resource")
	@Command(name = "time-travel", description = "TODO")
	public void timeTravel(@Mixin final LocalElectionResultParameter resultParameter,
			@Parameters(paramLabel = "FILE",
					description = DESCRIPTION_FILE_WRITE
							+ "\nWriting is done atomic to avoid blank browser screens."
							+ DESCRIPTION_OVERWRITE_FILE) final Path output,
			@Option(names = "--sleep", defaultValue = "1000", description = "TODO") final long sleep,
			@Option(names = "--step-size", defaultValue = "1", description = "TODO") final int stepSize,
			@Option(names = "--start", defaultValue = "0", description = "TODO") final int start,
			@Option(names = "--end", defaultValue = "100", description = "TODO") final int end)
			throws InterruptedException, IOException {
		final LocalElectionResult result = resultParameter.read();
		final Map<District<?>, OptionalInt> numberOfAllBallots = result.getElection()
				.getDistricts()
				.stream()
				.collect(toMap(identity(), result::getNumberOfAllBallots));

		for (int numberOfBallots = result.getBallots().size() * start / HUNDRED;
				numberOfBallots <= result.getBallots().size() * end / HUNDRED + stepSize - 1;
				numberOfBallots += stepSize) {
			final LocalElectionResult subResult = new LocalElectionResult(result.getElection(),
					result.getSainteLagueScale(),
					numberOfAllBallots,
					result.getDirectDrawResults(),
					result.getListDrawResults(),
					result.getBallots()
							.stream()
							.sorted((a, b) -> b.getPollingStation().compareTo(a.getPollingStation()))
							.limit(numberOfBallots)
							.collect(toList()));

			writePresentationFile(subResult, output);
			getStandardOutputWriter().println(String.format("Travelled to %5.1f%% at %2$tT %2$tZ",
					Math.min(100, (double) numberOfBallots * HUNDRED / result.getBallots().size()),
					ZonedDateTime.now()));
			Thread.sleep(sleep);
		}
	}

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
