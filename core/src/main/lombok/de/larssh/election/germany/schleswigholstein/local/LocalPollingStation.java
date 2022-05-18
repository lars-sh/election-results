package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wahlbezirk (§ 16 KomWG SH)
 */
@JsonIgnoreProperties("children")
@SuppressFBWarnings(value = "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY",
		justification = "no circular dependency, but a hierarchical")
public class LocalPollingStation extends District<District<?>> {
	@PackagePrivate
	LocalPollingStation(final LocalDistrict parent, final String name) {
		super(Optional.of(parent), name);
	}

	@Override
	public District<?> createChild(@SuppressWarnings("unused") final String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	@JsonIgnore
	public Optional<LocalDistrict> getParent() {
		return super.getParent().map(LocalDistrict.class::cast);
	}

	@Override
	@JsonIgnore
	public LocalDistrictRoot getRoot() {
		return (LocalDistrictRoot) super.getRoot();
	}
}
