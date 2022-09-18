package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wahlkreis (§ 15 GKWG)
 */
@SuppressFBWarnings(value = "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY",
		justification = "no circular dependency, but a hierarchical")
public class LocalDistrict extends District<LocalPollingStation> {
	/**
	 * Wahlkreis (§ 15 GKWG)
	 *
	 * @param parent Wahlgebiet
	 * @param name   Name
	 */
	@PackagePrivate
	LocalDistrict(final LocalDistrictRoot parent, final String name) {
		super(Optional.of(parent), name);
	}

	/** {@inheritDoc} */
	@Override
	public LocalPollingStation createChild(final String name) {
		return addChild(new LocalPollingStation(this, name));
	}

	/** {@inheritDoc} */
	@Override
	@JsonIgnore
	public Optional<LocalDistrictRoot> getParent() {
		return Optional.of(getRoot());
	}

	/** {@inheritDoc} */
	@Override
	@JsonIgnore
	public LocalDistrictRoot getRoot() {
		return (LocalDistrictRoot) super.getRoot();
	}
}
