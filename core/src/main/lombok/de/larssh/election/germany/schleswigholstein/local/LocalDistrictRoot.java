package de.larssh.election.germany.schleswigholstein.local;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public class LocalDistrictRoot extends District<LocalDistrict> {
	LocalDistrictType type;

	@JsonCreator(mode = Mode.DELEGATING)
	private LocalDistrictRoot(final ParsableLocalDistrictRoot parseable) {
		this(parseable.getName(), parseable.getType());

		parseable.createChildrenFor(this);
	}

	public LocalDistrictRoot(final String name, final LocalDistrictType type) {
		super(Optional.empty(), name);

		this.type = type;
	}

	@Override
	public LocalDistrict createChild(final String name) {
		return addChild(new LocalDistrict(this, name));
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

	@Getter
	private static class ParsableLocalDistrictRoot {
		final String name;

		final Set<ParsableLocalDistrict> children;

		final LocalDistrictType type;

		@SuppressWarnings("unused")
		public ParsableLocalDistrictRoot(@Nullable final String name,
				@Nullable final Set<ParsableLocalDistrict> children,
				@Nullable final LocalDistrictType type) {
			this.name = Nullables.orElseThrow(name);
			this.children = Nullables.orElseGet(children, Collections::emptySet);
			this.type = Nullables.orElseThrow(type);
		}

		public void createChildrenFor(final LocalDistrictRoot localDistrictRoot) {
			for (final ParsableLocalDistrict child : getChildren()) {
				child.createChildrenFor(localDistrictRoot.createChild(child.getName()));
			}
		}
	}

	@Getter
	private static class ParsableLocalDistrict {
		final String name;

		final Set<ParsableLocalPollingStation> children;

		@SuppressWarnings("unused")
		public ParsableLocalDistrict(@Nullable final String name,
				@Nullable final Set<ParsableLocalPollingStation> children) {
			this.name = Nullables.orElseThrow(name);
			this.children = Nullables.orElseGet(children, Collections::emptySet);
		}

		public void createChildrenFor(final LocalDistrict localDistrict) {
			getChildren().stream().map(ParsableLocalPollingStation::getName).forEach(localDistrict::createChild);
		}

		public Set<ParsableLocalPollingStation> getChildren() {
			return Nullables.orElseGet(children, Collections::emptySet);
		}
	}

	@Getter
	private static class ParsableLocalPollingStation {
		final String name;

		@JsonCreator(mode = Mode.PROPERTIES)
		public ParsableLocalPollingStation(@Nullable final String name) {
			this.name = Nullables.orElseThrow(name);
		}
	}
}
