package de.larssh.election.germany.schleswigholstein.local;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.utils.Nullables;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Wahlgebiet (§ 2 GKWG)
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LocalDistrictRoot extends District<LocalDistrict> {
	/**
	 * Art des Wahlgebiets
	 *
	 * @return Art des Wahlgebiets
	 */
	LocalDistrictType type;

	/**
	 * Wahlgebiet (§ 2 GKWG)
	 *
	 * @param parseable Jackson delegate
	 */
	@JsonCreator(mode = Mode.DELEGATING)
	@SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR",
			justification = "passing this to createChild, but made sure, it's done in last position")
	private LocalDistrictRoot(final ParsableLocalDistrictRoot parseable) {
		this(parseable.getName(), parseable.getType());

		for (final ParsableLocalDistrict parsableLocalDistrict : parseable.getChildren()) {
			final LocalDistrict localDistrict = createChild(parsableLocalDistrict.getName());

			for (final ParsableLocalPollingStation parsableLocalPollingStation : parsableLocalDistrict.getChildren()) {
				localDistrict.createChild(parsableLocalPollingStation.getName());
			}
		}
	}

	/**
	 * Wahlgebiet (§ 2 GKWG)
	 *
	 * @param name Name
	 * @param type Art des Wahlgebiets
	 */
	public LocalDistrictRoot(final String name, final LocalDistrictType type) {
		super(Optional.empty(), name);

		this.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public LocalDistrict createChild(final String name) {
		return addChild(new LocalDistrict(this, name));
	}

	/** {@inheritDoc} */
	@Override
	@JsonIgnore
	public Optional<? extends District<?>> getParent() {
		return super.getParent();
	}

	/** {@inheritDoc} */
	@Override
	@JsonIgnore
	public LocalDistrictRoot getRoot() {
		return (LocalDistrictRoot) super.getRoot();
	}

	/**
	 * Wahlgebiet (§ 2 GKWG)
	 */
	@Getter
	private static class ParsableLocalDistrictRoot {
		/**
		 * Name des Wahlgebiets
		 *
		 * @return Name
		 */
		final String name;

		/**
		 * Wahlkreise des Wahlgebiets
		 *
		 * @return Wahlkreise
		 */
		final Set<ParsableLocalDistrict> children;

		/**
		 * Art des Wahlgebiets
		 *
		 * @return Art des Wahlgebiets
		 */
		final LocalDistrictType type;

		/**
		 * Wahlgebiet (§ 2 GKWG)
		 *
		 * @param name     Name
		 * @param children Wahlkreise des Wahlgebiets
		 * @param type     Art des Wahlgebiets
		 */
		@PackagePrivate
		ParsableLocalDistrictRoot(@Nullable final String name,
				@Nullable final Set<ParsableLocalDistrict> children,
				@Nullable final LocalDistrictType type) {
			this.name = Nullables.orElseThrow(name,
					() -> new ElectionException("Missing required parameter \"name\" for local district root."));
			this.children = Nullables.orElseGet(children, Collections::emptySet);
			this.type = Nullables.orElseThrow(type,
					() -> new ElectionException("Missing required parameter \"type\" for root district."));
		}
	}

	/**
	 * Wahlkreis (§ 15 GKWG)
	 */
	@Getter
	private static class ParsableLocalDistrict {
		/**
		 * Name des Wahlkreises
		 *
		 * @return Name
		 */
		final String name;

		/**
		 * Wahlbezirke des Wahlkreises
		 */
		final Set<ParsableLocalPollingStation> children;

		/**
		 * Wahlkreis (§ 15 GKWG)
		 *
		 * @param name     Name
		 * @param children Wahlbezirke des Wahlkreises
		 */
		@PackagePrivate
		ParsableLocalDistrict(@Nullable final String name, @Nullable final Set<ParsableLocalPollingStation> children) {
			this.name = Nullables.orElseThrow(name,
					() -> new ElectionException("Missing required parameter \"name\" for local district."));
			this.children = Nullables.orElseGet(children, Collections::emptySet);
		}

		/**
		 * Wahlbezirke des Wahlkreises
		 *
		 * @return Wahlbezirke
		 */
		public Set<ParsableLocalPollingStation> getChildren() {
			return Nullables.orElseGet(children, Collections::emptySet);
		}
	}

	/**
	 * Wahlbezirk (§ 16 GKWG)
	 */
	@Getter
	private static class ParsableLocalPollingStation {
		/**
		 * Name des Wahlbezirks
		 *
		 * @return Name
		 */
		final String name;

		/**
		 * Wahlbezirk (§ 16 GKWG)
		 *
		 * @param name Name
		 */
		@JsonCreator(mode = Mode.PROPERTIES)
		private ParsableLocalPollingStation(@Nullable final String name) {
			this.name = Nullables.orElseThrow(name,
					() -> new ElectionException("Missing required parameter \"name\" for local polling station."));
		}
	}
}
