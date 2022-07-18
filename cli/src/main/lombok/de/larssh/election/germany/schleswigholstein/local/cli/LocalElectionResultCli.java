package de.larssh.election.germany.schleswigholstein.local.cli;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.OptionalInt;
import java.util.jar.Attributes.Name;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.file.AwgWebsiteFiles;
import de.larssh.election.germany.schleswigholstein.local.file.PresentationFiles;
import de.larssh.utils.io.Resources;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "election-results",
		mixinStandardHelpOptions = true,
		versionProvider = LocalElectionResultCli.class,
		description = "TODO")
public class LocalElectionResultCli implements IVersionProvider {
	/**
	 * One hundred
	 */
	private static final int HUNDRED = 100;

	private static final String WRITE_TO = "File to write to.";

	private static final String FILE_OVERWRITTEN = "\nIn case the file exists already it is overwritten.";

	@SuppressWarnings("checkstyle:UncommentedMain")
	public static void main(final String... args) {
		System.exit(new CommandLine(new LocalElectionResultCli()).execute(args));
	}

	@Command(name = "awg-website", description = "Creates a PHP file to be used for the AWG website.")
	public void awgWebsite(@Mixin final LocalElectionResultParams result,
			@Parameters(paramLabel = "FILE", description = WRITE_TO + FILE_OVERWRITTEN) final Path output)
			throws IOException {
		try (Writer writer = Files.newBufferedWriter(output)) {
			AwgWebsiteFiles.write(result.get(), writer);
		}
	}

	@Command(description = "Creates a HTML presentation format for the election result.")
	public void presentation(@Mixin final LocalElectionResultParams result,
			@Parameters(paramLabel = "FILE",
					description = WRITE_TO
							+ "\nWriting is done atomic to avoid blank browser screens."
							+ FILE_OVERWRITTEN) final Path output)
			throws IOException {
		writePresentationFile(result.get(), output);
	}

	@Command(name = "time-travel", description = "TODO")
	public void timeTravel(@Mixin final LocalElectionResultParams result,
			@Parameters(paramLabel = "FILE",
					description = WRITE_TO
							+ "\nWriting is done atomic to avoid blank browser screens."
							+ FILE_OVERWRITTEN) final Path output,
			@Option(names = "--sleep", defaultValue = "1000", description = "TODO") final int sleep,
			@Option(names = "--step-size", defaultValue = "1", description = "TODO") final int stepSize,
			@Option(names = "--start", defaultValue = "0", description = "TODO") final int start,
			@Option(names = "--end", defaultValue = "100", description = "TODO") final int end)
			throws InterruptedException, IOException {
		final Map<District<?>, OptionalInt> numberOfAllBallots = result.get()
				.getElection()
				.getDistricts()
				.stream()
				.collect(toMap(identity(), result.get()::getNumberOfAllBallots));

		for (int numberOfBallots = result.get().getBallots().size() * start / HUNDRED;
				numberOfBallots <= result.get().getBallots().size() * end / HUNDRED + stepSize - 1;
				numberOfBallots += stepSize) {
			final LocalElectionResult subResult = new LocalElectionResult(result.get().getElection(),
					result.get().getSainteLagueScale(),
					numberOfAllBallots,
					result.get().getDirectDrawResults(),
					result.get().getListDrawResults(),
					result.get()
							.getBallots()
							.stream()
							.sorted((a, b) -> b.getPollingStation().compareTo(a.getPollingStation()))
							.limit(numberOfBallots)
							.collect(toList()));

			writePresentationFile(subResult, output);

			Thread.sleep(sleep);
		}
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

	@Override
	public String[] getVersion() throws IOException {
		return new String[] {
				Resources.readManifest(getClass())
						.map(manifest -> manifest.getMainAttributes().get(Name.IMPLEMENTATION_VERSION).toString())
						.orElse("unknown") };
	}
}
