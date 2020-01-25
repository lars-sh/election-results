package de.larssh.election.germany.schleswigholstein;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import de.larssh.utils.Optionals;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public abstract class District<C extends District<?>> implements Comparable<District<?>> {
	private static final Comparator<District<?>> COMPARATOR
			= Comparator.<District<?>, Optional<District<?>>>comparing(District::getParent, Optionals.comparator())
					.thenComparing(District::getName);

	@EqualsAndHashCode.Include
	@ToString.Include(rank = -1)
	Optional<District<?>> parent;

	@EqualsAndHashCode.Include
	String name;

	@ToString.Exclude
	Set<C> children = new TreeSet<>();

	@Override
	public int compareTo(@Nullable final District<?> district) {
		return COMPARATOR.compare(this, district);
	}

	protected C addChild(final C child) {
		children.add(child);
		return child;
	}

	public abstract C createChild(String name);

	public Set<C> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	public District<?> getRoot() {
		return getParent().<District<?>>map(District::getRoot).orElse(this);
	}
}
