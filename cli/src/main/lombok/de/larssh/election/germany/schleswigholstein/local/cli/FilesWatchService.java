package de.larssh.election.germany.schleswigholstein.local.cli;

import static de.larssh.utils.Collectors.toLinkedHashMap;
import static java.util.Collections.synchronizedSet;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.collection.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class wraps {@link WatchService} to watch files instead of directories.
 * It is thread-safe.
 *
 * <p>
 * For now only files of the same {@link java.nio.file.FileSystem} can be
 * watched.
 */
@PackagePrivate
@RequiredArgsConstructor
class FilesWatchService implements Closeable {
	/**
	 * A set of all files to watch.
	 */
	Set<Path> filesToWatch = synchronizedSet(new HashSet<>());

	/**
	 * Object used for locking
	 */
	Object lock = new Object();

	/**
	 * The wrapped {@link WatchService}, which is created lazily to choose the
	 * correct {@link java.nio.file.FileSystem}.
	 */
	AtomicReference<WatchService> watchService = new AtomicReference<>();

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings({ "checkstyle:SuppressWarnings", "PMD.CloseResource" })
	public void close() throws IOException {
		@SuppressWarnings("resource")
		final WatchService watchService = this.watchService.get();
		if (watchService != null) {
			watchService.close();
		}
	}

	/**
	 * Retrieves the wrapped {@link WatchService}. If not present, it is created
	 * based on {@code path.getFileSystem()}.
	 *
	 * @param path the path to determine the correct
	 *             {@link java.nio.file.FileSystem} of
	 * @return the wrapped {@link WatchService}
	 * @throws IOException on IO error
	 */
	@SuppressWarnings({ "checkstyle:SuppressWarnings", "resource" })
	private WatchService getWatchService(final Path path) throws IOException {
		final WatchService watchService = this.watchService.get();
		if (watchService != null) {
			return watchService;
		}

		synchronized (lock) {
			if (this.watchService.get() == null) {
				this.watchService.set(path.getFileSystem().newWatchService());
			}
			return this.watchService.get();
		}
	}

	/**
	 * Registers {@code file} to be watched for the given set of {@code kinds}.
	 *
	 * @param file  the file to watch
	 * @param kinds the kinds to watch
	 * @return the created {@link WatchKey}
	 * @throws IOException on IO error
	 */
	@SuppressWarnings({ "checkstyle:SuppressWarnings", "resource" })
	public WatchKey register(final Path file, final WatchEvent.Kind<?>... kinds) throws IOException {
		final Path parentFolder = file.toAbsolutePath().getParent();
		if (parentFolder == null) {
			throw new IllegalArgumentException(
					String.format("Path \"%s\" seems to be no file, as there is no parent folder.", file));
		}

		// It's not possible to watch single files.
		// Therefore we watch the parent directory of all input files.
		final WatchKey key = parentFolder.register(getWatchService(file), kinds);

		// Registering might fail, therefore add to the set after registration
		filesToWatch.add(file.toAbsolutePath().normalize());
		return key;
	}

	/**
	 * Waits for the next watch event and returns it. The returned
	 * {@link FileWatchResult} needs to be closed to release the inner
	 * {@link WatchKey} and allow it to be watched again.
	 *
	 * <p>
	 * This method works similar to {@link WatchService#take()}.
	 *
	 * @return the next watch event
	 * @throws InterruptedException if interrupted while waiting
	 */
	@SuppressWarnings({ "checkstyle:SuppressWarnings", "unchecked" })
	@SuppressFBWarnings(value = "FII_USE_FUNCTION_IDENTITY",
			justification = "WatchEvent.class::cast cannot be used here, as it does not support generics")
	public FileWatchResult watch() throws InterruptedException {
		// Wait for the next watch key
		@SuppressWarnings("resource")
		final WatchKey key = watchService.get().take();

		final Path watchable = (Path) key.watchable();
		final Map<Path, WatchEvent<Path>> events = key.pollEvents()
				.stream()
				.map(event -> (WatchEvent<Path>) event)
				.map(event -> Maps.entry(watchable.resolve(event.context()).toAbsolutePath().normalize(), event))
				.filter(entry -> filesToWatch.contains(entry.getKey()))
				.collect(toLinkedHashMap());

		if (!events.isEmpty()) {
			return new FileWatchResult(key, events);
		}

		key.reset();
		return watch();
	}

	/**
	 * This class contains a set of watch events and the watched files absolute
	 * paths.
	 */
	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	public static class FileWatchResult implements AutoCloseable {
		/**
		 * The current watch key
		 *
		 * @return the current watch key
		 */
		WatchKey key;

		/**
		 * The current watch events by the watched files absolute paths
		 *
		 * @return the current watch events by the watched files absolute paths
		 */
		Map<Path, WatchEvent<Path>> events;

		/** {@inheritDoc} */
		@Override
		public void close() {
			getKey().reset();
		}
	}
}
