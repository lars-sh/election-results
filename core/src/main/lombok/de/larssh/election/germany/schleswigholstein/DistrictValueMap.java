package de.larssh.election.germany.schleswigholstein;

import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;

import de.larssh.utils.Nullables;
import de.larssh.utils.collection.ProxiedMap;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A map to store numeric values per {@link District}. When retrieving data of a
 * higher-level district its value is calculated based on the sum of the values
 * of its children.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DistrictValueMap extends ProxiedMap<District<?>, OptionalInt> {
	/**
	 * The election to provide available {@link District}s
	 */
	@EqualsAndHashCode.Include
	Election<?, ?> election;

	/**
	 * A map to store numeric values per {@link District}. When retrieving data of a
	 * higher-level district its value is calculated based on its children.
	 *
	 * @param election the election providing available {@link District}s
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Known problem")
	public DistrictValueMap(final Election<?, ?> election) {
		super(new TreeMap<>());

		this.election = election;
	}

	/**
	 * Retrieves the value for {@code district}. For higher-level district its value
	 * is calculated based on the sum of the values of its children.
	 *
	 * @param district the district to retrieve information about
	 * @return the calculated value or empty
	 */
	@SuppressFBWarnings(value = "OI_OPTIONAL_ISSUES_CHECKING_REFERENCE", justification = "optimized map contains")
	public OptionalInt get(final District<?> district) {
		final OptionalInt value = super.get(district);
		if (value != null && value.isPresent()) {
			return value;
		}

		final Set<? extends District<?>> children = district.getChildren();
		if (children.isEmpty()) {
			return OptionalInt.empty();
		}

		int calculated = 0;
		for (final District<?> child : children) {
			final OptionalInt valueOfChild = get(child);
			if (!valueOfChild.isPresent()) {
				return OptionalInt.empty();
			}
			calculated += valueOfChild.getAsInt();
		}
		return OptionalInt.of(calculated);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isModifiable() {
		return true;
	}

	/** {@inheritDoc} */
	@Nullable
	@Override
	public OptionalInt put(@Nullable final District<?> nullableDistrict, @Nullable final OptionalInt value) {
		final District<?> district = Nullables.orElseThrow(nullableDistrict);

		if (!district.getRoot().equals(election.getDistrict())) {
			throw new ElectionException("District \"%s\" is not part of the elections district hierarchy.",
					district.getKey());
		}
		if (super.containsKey(district)) {
			throw new ElectionException("The value has already been set for district \"%s\".", district.getKey());
		}
		return super.put(district, Nullables.orElseThrow(value));
	}

	/** {@inheritDoc} */
	@Override
	public void putAll(@Nullable final Map<? extends District<?>, ? extends OptionalInt> map) {
		for (final Entry<? extends District<?>, ? extends OptionalInt> entry : Nullables.orElseThrow(map).entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Copies all elements of {@code map}. {@link District}s are identified by key.
	 *
	 * @param map mappings to be stored in this map
	 */
	public void putAllByKey(final Map<String, OptionalInt> map) {
		put(election.getDistrict(), map.getOrDefault(election.getDistrict(), OptionalInt.empty()));
		for (final District<?> child : election.getDistrict().getAllChildren()) {
			put(child, map.getOrDefault(child.getKey(), OptionalInt.empty()));
		}
	}
}
