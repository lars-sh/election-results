package de.larssh.election.germany.schleswigholstein;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.utils.Optionals;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.NonFinal;

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
	 * Background color to use in diagrams
	 *
	 * @param backgroundColor the background color
	 * @return the background color
	 */
	@Setter
	@NonFinal
	Color backgroundColor = Color.BLACK;

	/**
	 * Font color to use in diagrams
	 *
	 * @param fontColor the font color
	 * @return the font color
	 */
	@Setter
	@NonFinal
	Color fontColor = Color.WHITE;

	/**
	 * Children of this district
	 */
	@ToString.Exclude
	Set<C> children = new LinkedHashSet<>();

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
	 * Checks if {@code district} is part of the current district's hierarchy.
	 *
	 * @param district the district to search for
	 * @return {@code true} if {@code district} is a child or equal to this
	 *         district, else {@code false}
	 */
	public boolean contains(final District<?> district) {
		return equals(district) || district.getParent().filter(this::contains).isPresent();
	}

	/**
	 * Collects all children of this district recursively.
	 *
	 * @return all children
	 */
	@JsonIgnore
	public Set<District<?>> getAllChildren() {
		final Set<District<?>> allChildren = new LinkedHashSet<>();
		for (final C child : getChildren()) {
			allChildren.add(child);
			allChildren.addAll(child.getAllChildren());
		}
		return allChildren;
	}

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
