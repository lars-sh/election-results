package de.larssh.election.germany.schleswigholstein.local;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.larssh.election.germany.schleswigholstein.Color;
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
@SuppressWarnings("PMD.DataClass")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LocalDistrictRoot extends District<LocalDistrict> {
	/**
	 * Art des Wahlgebiets
	 *
	 * @return Art des Wahlgebiets
	 */
	@JsonProperty(index = 0)
	LocalDistrictType type;

	/**
	 * Wahlgebiet (§ 2 GKWG)
	 *
	 * @param parseable Jackson delegate
	 */
	@JsonCreator(mode = Mode.DELEGATING)
	@SuppressFBWarnings(value = "PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS",
			justification = "passing this to createChild, but made sure, it's done in last position")
	private LocalDistrictRoot(final ParsableLocalDistrictRoot parseable) {
		this(parseable.getName(), parseable.getType());
		setBackgroundColor(parseable.getBackgroundColor());
		setFontColor(parseable.getFontColor());

		for (final ParsableLocalDistrict parsableLocalDistrict : parseable.getChildren()) {
			final LocalDistrict localDistrict = createChild(parsableLocalDistrict.getName());
			localDistrict.setBackgroundColor(parsableLocalDistrict.getBackgroundColor());
			localDistrict.setFontColor(parsableLocalDistrict.getFontColor());

			for (final ParsableLocalPollingStation parsableLocalPollingStation : parsableLocalDistrict.getChildren()) {
				final LocalPollingStation pollingStation
						= localDistrict.createChild(parsableLocalPollingStation.getName());
				pollingStation.setBackgroundColor(parsableLocalPollingStation.getBackgroundColor());
				pollingStation.setFontColor(parsableLocalPollingStation.getFontColor());
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
		 * Art des Wahlgebiets
		 *
		 * @return Art des Wahlgebiets
		 */
		final LocalDistrictType type;

		/**
		 * Background color to use in diagrams
		 *
		 * @return the background color
		 */
		Color backgroundColor;

		/**
		 * Font color to use in diagrams
		 *
		 * @return the font color
		 */
		Color fontColor;

		/**
		 * Wahlkreise des Wahlgebiets
		 *
		 * @return Wahlkreise
		 */
		final Set<ParsableLocalDistrict> children;

		/**
		 * Wahlgebiet (§ 2 GKWG)
		 *
		 * @param name            Name
		 * @param type            Art des Wahlgebiets
		 * @param backgroundColor the background color
		 * @param fontColor       the font color
		 * @param children        Wahlkreise des Wahlgebiets
		 */
		@PackagePrivate
		@SuppressWarnings("PMD.LooseCoupling")
		ParsableLocalDistrictRoot(@Nullable final String name,
				@Nullable final LocalDistrictType type,
				@Nullable final Color backgroundColor,
				@Nullable final Color fontColor,
				@Nullable final LinkedHashSet<ParsableLocalDistrict> children) {
			this.name = Nullables.orElseThrow(name,
					() -> new ElectionException("Missing required parameter \"name\" for local district root."));
			this.children = Nullables.orElseGet(children, Collections::emptySet);
			this.type = Nullables.orElseThrow(type,
					() -> new ElectionException("Missing required parameter \"type\" for root district."));
			this.backgroundColor = Nullables.orElse(backgroundColor, Color.BLACK);
			this.fontColor = Nullables.orElse(fontColor, Color.WHITE);
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
		 * Background color to use in diagrams
		 *
		 * @return the background color
		 */
		Color backgroundColor;

		/**
		 * Font color to use in diagrams
		 *
		 * @return the font color
		 */
		Color fontColor;

		/**
		 * Wahlbezirke des Wahlkreises
		 */
		Set<ParsableLocalPollingStation> children;

		/**
		 * Wahlkreis (§ 15 GKWG)
		 *
		 * @param name            Name
		 * @param backgroundColor the background color
		 * @param fontColor       the font color
		 * @param children        Wahlbezirke des Wahlkreises
		 */
		@PackagePrivate
		@SuppressWarnings("PMD.LooseCoupling")
		ParsableLocalDistrict(@Nullable final String name,
				@Nullable final Color backgroundColor,
				@Nullable final Color fontColor,
				@Nullable final LinkedHashSet<ParsableLocalPollingStation> children) {
			this.name = Nullables.orElseThrow(name,
					() -> new ElectionException("Missing required parameter \"name\" for local district."));
			this.backgroundColor = Nullables.orElse(backgroundColor, Color.BLACK);
			this.fontColor = Nullables.orElse(fontColor, Color.WHITE);
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
		 * Background color to use in diagrams
		 *
		 * @return the background color
		 */
		Color backgroundColor;

		/**
		 * Font color to use in diagrams
		 *
		 * @return the font color
		 */
		Color fontColor;

		/**
		 * Wahlbezirk (§ 16 GKWG)
		 *
		 * @param name            Name
		 * @param backgroundColor the background color
		 * @param fontColor       the font color
		 */
		@JsonCreator(mode = Mode.PROPERTIES)
		private ParsableLocalPollingStation(@Nullable final String name,
				@Nullable final Color backgroundColor,
				@Nullable final Color fontColor) {
			this.name = Nullables.orElseThrow(name,
					() -> new ElectionException("Missing required parameter \"name\" for local polling station."));
			this.backgroundColor = Nullables.orElse(backgroundColor, Color.BLACK);
			this.fontColor = Nullables.orElse(fontColor, Color.WHITE);
		}
	}
}
