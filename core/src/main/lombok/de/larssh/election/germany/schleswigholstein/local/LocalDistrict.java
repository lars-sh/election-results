package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.utils.annotations.PackagePrivate;

public class LocalDistrict extends District<LocalPollingStation> {
	@PackagePrivate
	LocalDistrict(final LocalDistrictRoot parent, final String name) {
		super(Optional.of(parent), name);
	}

	@Override
	public LocalPollingStation createChild(final String name) {
		return addChild(new LocalPollingStation(this, name));
	}

	@Override
	@JsonIgnore
	public Optional<District<?>> getParent() {
		return super.getParent();
	}

	@Override
	@JsonIgnore
	public LocalDistrictRoot getRoot() {
		return (LocalDistrictRoot) super.getRoot();
	}
}
