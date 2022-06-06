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

/**
 * Generic representation of a district, which can be split into children.
 *
 * @param <C> the type of children
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class District<C extends District<?>> implements Comparable<District<?>> {
	/**
	 * Comparator by parent and name
	 */
	private static final Comparator<District<?>> COMPARATOR = Comparator
			.<District<?>, Optional<? extends District<?>>>comparing(District::getParent, Optionals.comparator())
			.thenComparing(District::getName);

	/**
	 * Parent of the district or empty for root districts
	 *
	 * @return the parent district or empty
	 */
	@EqualsAndHashCode.Include
	@ToString.Include(rank = -1)
	Optional<? extends District<?>> parent;

	/**
	 * Name of the district
	 *
	 * @return the name
	 */
	@EqualsAndHashCode.Include
	String name;

	/**
	 * Children of this district
	 */
	@ToString.Exclude
	Set<C> children = new TreeSet<>();

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nullable final District<?> district) {
		return COMPARATOR.compare(this, district);
	}

	/**
	 * Registers a child
	 *
	 * @param child the child
	 * @return the new child to allow chaining
	 */
	protected C addChild(final C child) {
		if (getChildren().stream().map(District::getName).anyMatch(name::equals)) {
			final String key = getKey();
			throw new ElectionException("Another district with name \"%s\" already exists as child of \"%s\".",
					child.getName(),
					key.isEmpty() ? getName() : key);
		}
		children.add(child);
		return child;
	}

	/**
	 * Creates and registers a new child using the given name
	 *
	 * @param name the name
	 * @return the new child
	 */
	public abstract C createChild(String name);

	/**
	 * Children of this district
	 *
	 * @return the children
	 */
	public Set<C> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	/**
	 * Creates a unique key based on the names of the parents and this.
	 *
	 * <p>
	 * The name of root districts is omitted.
	 *
	 * @return unique key based on the names
	 */
	@JsonIgnore
	public String getKey() {
		return getParent().map(parent -> Keys.escape(parent.getKey(), ", ", getName(), "")).orElse("");
	}

	/**
	 * Traverses the parents to find the root district
	 *
	 * @return the root district
	 */
	public District<?> getRoot() {
		return getParent().<District<?>>map(District::getRoot).orElse(this);
	}
}
