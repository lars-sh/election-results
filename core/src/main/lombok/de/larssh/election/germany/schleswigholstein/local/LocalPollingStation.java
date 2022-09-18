package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wahlbezirk (§ 16 GKWG)
 */
@JsonIgnoreProperties("children")
@SuppressFBWarnings(value = "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY",
		justification = "no circular dependency, but a hierarchical")
public class LocalPollingStation extends District<District<?>> {
	/**
	 * Wahlbezirk (§ 16 GKWG)
	 *
	 * @param parent Wahlkreis
	 * @param name   Name
	 */
	@PackagePrivate
	LocalPollingStation(final LocalDistrict parent, final String name) {
		super(Optional.of(parent), name);
	}

	/** {@inheritDoc} */
	@Override
	public District<?> createChild(@SuppressWarnings("unused") final String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@link LocalDistrict} of this {@link LocalPollingStation}
	 *
	 * @return the {@link LocalDistrict} of this {@link LocalPollingStation}
	 */
	@JsonIgnore
	public LocalDistrict getDistrict() {
		return getParent().get();
	}

	/** {@inheritDoc} */
	@Override
	@JsonIgnore
	public Optional<LocalDistrict> getParent() {
		return super.getParent().map(LocalDistrict.class::cast);
	}

	/** {@inheritDoc} */
	@Override
	@JsonIgnore
	public LocalDistrictRoot getRoot() {
		return (LocalDistrictRoot) super.getRoot();
	}
}
