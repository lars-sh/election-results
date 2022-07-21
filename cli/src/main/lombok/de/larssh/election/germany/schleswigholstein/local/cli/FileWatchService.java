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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// The Java WatchService is used to be notified upon file changes
@PackagePrivate
class FileWatchService implements Closeable {
	Set<Path> filesToWatch = synchronizedSet(new HashSet<>());

	AtomicReference<WatchService> watchService = new AtomicReference<>();

	@Override
	public void close() throws IOException {
		final WatchService watchService = this.watchService.get();
		if (watchService != null) {
			watchService.close();
		}
	}

	@SuppressWarnings("resource")
	private WatchService getWatchService(final Path path) throws IOException {
		final WatchService watchService = this.watchService.get();
		if (watchService != null) {
			return watchService;
		}

		synchronized (this.watchService) {
			if (this.watchService.get() == null) {
				this.watchService.set(path.getFileSystem().newWatchService());
			}
			return this.watchService.get();
		}
	}

	@SuppressWarnings("resource")
	public WatchKey register(final Path file, final WatchEvent.Kind<?>... kinds) throws IOException {
		// It's not possible to watch single files.
		// Therefore we watch the parent directory of all input files.
		final WatchKey key = file.getParent().register(getWatchService(file), kinds);

		// Registering might fail, therefore add to the set after registration
		filesToWatch.add(file.toAbsolutePath().normalize());
		return key;
	}

	@SuppressWarnings("unchecked")
	public FileWatchResult watch() throws InterruptedException {
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

	@Getter
	@RequiredArgsConstructor
	public static class FileWatchResult implements AutoCloseable {
		WatchKey key;

		Map<Path, WatchEvent<Path>> events;

		/** {@inheritDoc} */
		@Override
		public void close() {
			getKey().reset();
		}
	}
}
