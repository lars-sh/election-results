package de.larssh.election.germany.schleswigholstein.local.cli;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.election.germany.schleswigholstein.local.cli.FilesWatchService.FileWatchResult;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFileLineParseException;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFileParseException;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFiles;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
	@Option(names = { "-s", "--sainte-lague-scale" },
			paramLabel = "<Number>",
			description = "Scale (decimal places) of Sainte Laguë values")
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

	/**
	 * Current {@link CommandSpec} instance
	 *
	 * @return the current {@link CommandSpec} instance
	 */
	private CommandSpec getCommandSpec() {
		return Nullables.orElseThrow(commandSpec);
	}

	/**
	 * Path to the election data
	 *
	 * @return the path to the election data
	 */
	private Path getElectionPath() {
		return Nullables.orElseThrow(electionPath);
	}

	/**
	 * Any number of key-value pairs with the polling station as key and a path to
	 * the corresponding result file as value
	 *
	 * @return the result paths per polling station
	 */
	public Map<String, Path> getResultPaths() {
		return unmodifiableMap(resultPaths);
	}

	/**
	 * Watches the election and results files for changes and executes
	 * {@code handler} passing updated {@link LocalElectionResult}. This method
	 * loops endlessly and does not return control except an exception is thrown.
	 *
	 * <p>
	 * Results are read using {@link #read()}. Therefore some specific parsing
	 * errors might be written to standard out while processing continues with a
	 * probably incomplete result.
	 *
	 * @param handler the consumer handling the latest {@link LocalElectionResult}
	 * @throws InterruptedException if interrupted while watching for file changes
	 * @throws IOException          on IO error
	 */
	public void watch(final Consumer<LocalElectionResult> handler) throws InterruptedException, IOException {
		try (FilesWatchService fileWatchService = new FilesWatchService()) {
			fileWatchService.register(getElectionPath(), ENTRY_CREATE, ENTRY_MODIFY);
			for (final Path path : getResultPaths().values()) {
				fileWatchService.register(path, ENTRY_CREATE, ENTRY_MODIFY);
			}

			// Loop endlessly
			while (true) {
				try (FileWatchResult fileWatchResult = fileWatchService.watch()) {
					handler.accept(read());
				}
			}
		}
	}

	/**
	 * Dummy to avoid the IDE to mark some fields as {@code final}.
	 */
	@SuppressWarnings({ "PMD.NullAssignment", "PMD.UnusedPrivateMethod" })
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "dummy method")
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
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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
	@SuppressFBWarnings(value = "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE",
			justification = "There should be no risk by the exposure of internal information to the user here.")
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
								path.getFileName(),
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
}
