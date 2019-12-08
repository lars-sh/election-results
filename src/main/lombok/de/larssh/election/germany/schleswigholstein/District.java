package de.larssh.election.germany.schleswigholstein;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import de.larssh.utils.Optionals;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class District<C extends District<C>> implements Comparable<District<?>> {
	private static final Comparator<District<?>> COMPARATOR = Comparator
			.<District<?>, Optional<District<?>>>comparing(District::getParent, Optionals.createComparator())
			.thenComparing(District::getName);

	@EqualsAndHashCode.Include
	Optional<District<?>> parent;

	@EqualsAndHashCode.Include
	String name;

	@ToString.Exclude
	Set<C> children = new TreeSet<>();

	public District(final String name) {
		parent = Optional.empty();
		this.name = name;
	}

	public District(final District<C> parent, final String name) {
		this.parent = Optional.of(parent);
		this.name = name;
	}

	@Override
	public int compareTo(final District<?> district) {
		return COMPARATOR.compare(this, district);
	}

	protected C addChild(final C child) {
		children.add(child);
		return child;
	}

	public abstract C createChild(final String name);

	public Set<District<C>> getChildren() {
		return Collections.unmodifiableSet(children);
	}
}
