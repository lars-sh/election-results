package de.larssh.election.germany.schleswigholstein.local.cli;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.Attributes.Name;

import de.larssh.election.germany.schleswigholstein.local.file.AwgWebsiteFiles;
import de.larssh.election.germany.schleswigholstein.local.file.PresentationFiles;
import de.larssh.utils.io.Resources;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = "election-results",
		mixinStandardHelpOptions = true,
		versionProvider = LocalElectionResultCli.class,
		description = "TODO")
public class LocalElectionResultCli implements IVersionProvider {
	private static final String WRITE_TO = "File to write to.";

	private static final String FILE_OVERWRITTEN = "\nIn case the file exists already it is overwritten.";

	public static void main(final String... args) throws Exception {
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
							+ "\nWriting is done in atomic to avoid blank browser screens."
							+ FILE_OVERWRITTEN) final Path output)
			throws IOException {
		final Path tempFile
				= Files.createTempFile(output.toAbsolutePath().getParent(), output.getFileName().toString(), ".tmp");
		tempFile.toFile().deleteOnExit();

		try (Writer writer = Files.newBufferedWriter(tempFile)) {
			PresentationFiles.write(result.get(), writer);
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
