package de.larssh.election.germany.schleswigholstein;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.utils.Optionals;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class District<C extends District<?>> implements Comparable<District<?>> {
	private static final Comparator<District<?>> COMPARATOR = Comparator
			.<District<?>, Optional<? extends District<?>>>comparing(District::getParent, Optionals.comparator())
			.thenComparing(District::getName);

	private static final String KEY_SEPARATOR = ", ";

	@EqualsAndHashCode.Include
	@ToString.Include(rank = -1)
	Optional<? extends District<?>> parent;

	@EqualsAndHashCode.Include
	String name;

	@ToString.Exclude
	Set<C> children = new TreeSet<>();

	@Override
	public int compareTo(@Nullable final District<?> district) {
		return COMPARATOR.compare(this, district);
	}

	protected C addChild(final C child) {
		if (getChildren().stream().map(District::getName).anyMatch(name::equals)) {
			throw new ElectionException("Another district with name \"%s\" already exists as child of \"%s\".",
					child.getName(),
					getFullKey());
		}
		children.add(child);
		return child;
	}

	public abstract C createChild(String name);

	public Set<C> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	private String getFullKey() {
		return getParent().map(District::getFullKey).map(fullKey -> fullKey + KEY_SEPARATOR).orElse("")
				+ Keys.escape(getName(), ',', ' ');
	}

	@JsonIgnore
	public String getKey() {
		return getParent().map(parent -> {
			final StringBuilder builder = new StringBuilder(parent.getKey());
			if (builder.length() > 0 || getName().isEmpty()) {
				builder.append(KEY_SEPARATOR);
			}
			builder.append(Keys.escape(getName(), ',', ' '));
			return builder.toString();
		}).orElse("");
	}

	public District<?> getRoot() {
		return getParent().<District<?>>map(District::getRoot).orElse(this);
	}
}
