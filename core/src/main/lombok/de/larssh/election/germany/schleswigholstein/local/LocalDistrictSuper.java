package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;

import de.larssh.election.germany.schleswigholstein.District;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public class LocalDistrictSuper extends District<LocalDistrict> {
	LocalDistrictType type;

	public LocalDistrictSuper(final String name, final LocalDistrictType type) {
		super(Optional.empty(), name);

		this.type = type;
	}

	@Override
	public LocalDistrict createChild(final String name) {
		return addChild(new LocalDistrict(this, name));
	}
}
