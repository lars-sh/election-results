package de.larssh.election.germany.schleswigholstein.local.cli;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.election.germany.schleswigholstein.local.cli.FileWatchService.FileWatchResult;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFileLineParseException;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFileParseException;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFiles;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * A picocli compatible parameter set to load election results via command line
 * options.
 */
@RequiredArgsConstructor
public class LocalElectionResultParameter {
	/**
	 * Path to the election data
	 */
	@NonFinal
	@Nullable
	@Option(names = { "-e", "--election" },
			paramLabel = "<Path>",
			required = true,
			description = "Path to the election data")
	Path electionPath = null;

	/**
	 * Scale (decimal places) of Sainte Laguë values
	 */
	@NonFinal
	@Getter(AccessLevel.PRIVATE)
	@Option(names = { "-s", "--sainte-lague-scale" }, description = "Scale (decimal places) of Sainte Laguë values")
	int sainteLagueScale = 2;

	/**
	 * Any number of key-value pairs with the polling station as key and a path to
	 * the corresponding result file as value
	 */
	@NonFinal
	@Option(names = { "-R", "--result" },
			paramLabel = "<Polling Station>=<Path>",
			description = "Any number of key-value pairs with the polling station as key and a path to the corresponding result file as value")
	Map<String, Path> resultPaths = emptyMap();

	/**
	 * Current {@link CommandSpec} instance
	 */
	@Spec
	@NonFinal
	@Nullable
	CommandSpec commandSpec = null;

	private CommandSpec getCommandSpec() {
		return Nullables.orElseThrow(commandSpec);
	}

	private Path getElectionPath() {
		return Nullables.orElseThrow(electionPath);
	}

	public Map<String, Path> getResultPaths() {
		return resultPaths;
	}

	public void handle(final boolean loop, final LocalElectionResultHandler resultHandler)
			throws InterruptedException, IOException {
		resultHandler.accept(read());
		if (!loop) {
			return;
		}

		try (final FileWatchService fileWatchService = new FileWatchService()) {
			// Register files to watch
			fileWatchService.register(getElectionPath(), ENTRY_CREATE, ENTRY_MODIFY);
			for (final Path path : getResultPaths().values()) {
				fileWatchService.register(path, ENTRY_CREATE, ENTRY_MODIFY);
			}

			// Loop endlessly
			while (true) {
				try (final FileWatchResult fileWatchResult = fileWatchService.watch()) {
					resultHandler.accept(read());
				}
			}
		}
	}

	/**
	 * Dummy to avoid the IDE to mark some fields as {@code final}.
	 */
	@SuppressWarnings("unused")
	private void nonFinalDummy() {
		electionPath = null;
		sainteLagueScale = 2;
		resultPaths = emptyMap();
		commandSpec = null;
	}

	/**
	 * Reads all results specified by {@link #resultPaths} and merges them all
	 * together.
	 *
	 * @return one result containing multiple results
	 * @throws IOException on IO error
	 */
	public LocalElectionResult read() throws IOException {
		// Read Election
		final LocalElection election;
		try (Reader reader = Files.newBufferedReader(getElectionPath())) {
			election = LocalElection.fromJson(reader);
		}

		// Read all Election Results
		LocalElectionResult result = new LocalElectionResult(election,
				getSainteLagueScale(),
				emptyMap(),
				emptySet(),
				emptySet(),
				emptyList());
		for (final Entry<String, Path> resultPath : getResultPaths().entrySet()) {
			// Find Polling Station
			final LocalPollingStation pollingStation = election.getDistrict()
					.getChildren()
					.stream()
					.map(LocalDistrict::getChildren)
					.flatMap(Collection::stream)
					.filter(district -> resultPath.getKey().equals(district.getName()))
					.findAny()
					.orElseThrow(() -> new ElectionException("Cannot find a polling station named \"%s\".",
							resultPath.getKey()));

			// Read Election Results of Polling Station
			result = result.add(readSingleResult(election, pollingStation, resultPath.getValue()));
		}
		return result;
	}

	/**
	 * Reads a single election result by {@code path}.
	 *
	 * @param election       the election
	 * @param pollingStation the polling station to read results for
	 * @param path           the path to the polling station results file to load
	 * @return the loaded result
	 * @throws IOException on IO error
	 */
	@SuppressWarnings({ "checkstyle:SuppressWarnings", "resource" })
	private LocalElectionResult readSingleResult(final LocalElection election,
			final LocalPollingStation pollingStation,
			final Path path) throws IOException {
		try (Reader reader = Files.newBufferedReader(path)) {
			return PollingStationResultFiles.read(election, pollingStation, reader);
		} catch (final PollingStationResultFileParseException e) {
			for (final PollingStationResultFileLineParseException lineException : e
					.getSuppressedLineParseExceptions()) {
				getCommandSpec().commandLine()
						.getErr()
						.println(String.format("Line %d of \"%s\": %-70s | %s",
								lineException.getLineNumber(),
								path.getFileName().toString(),
								lineException.getMessage(),
								lineException.getLineContent()));

				final Throwable cause = lineException.getCause();
				if (cause != null && !(cause instanceof ElectionException)) {
					cause.printStackTrace(getCommandSpec().commandLine().getErr());
				}
			}
			return e.getIncompleteResult();
		}
	}

	@FunctionalInterface
	public interface LocalElectionResultHandler {
		void accept(LocalElectionResult result) throws IOException;
	}
}
