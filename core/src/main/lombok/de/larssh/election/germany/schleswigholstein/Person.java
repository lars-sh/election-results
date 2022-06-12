package de.larssh.election.germany.schleswigholstein;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Person with mandatory given and family name and optional details.
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Person implements Comparable<Person> {
	private static final Comparator<Person> COMPARATOR
			= Comparator.comparing(Person::getFamilyName).thenComparing(Person::getGivenName);

	public static String createKey(final String familyName, final String givenName) {
		return Keys.escape(familyName, ", ", givenName, "");
	}

	/**
	 * Given name
	 *
	 * @return the given name
	 */
	@EqualsAndHashCode.Include
	String givenName;

	/**
	 * Family name
	 *
	 * @return the family name
	 */
	@EqualsAndHashCode.Include
	String familyName;

	/**
	 * Optional gender
	 *
	 * @return the optional gender
	 */
	Optional<Gender> gender;

	/**
	 * Optional year of birth
	 *
	 * @return the optional year of birth
	 */
	OptionalInt yearOfBirth;

	/**
	 * Optional nationality
	 *
	 * @return the optional nationality
	 */
	Optional<Locale> nationality;

	/**
	 * Optional address
	 *
	 * @return the optional address
	 */
	Optional<Address> address;

	/**
	 * Optional job
	 *
	 * @return the optional job
	 */
	Optional<String> job;

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nullable final Person party) {
		return COMPARATOR.compare(this, party);
	}

	/**
	 * Creates a unique key based on the family and given name.
	 *
	 * @return unique key based on the family and given name
	 */
	@JsonIgnore
	public String getKey() {
		return createKey(getFamilyName(), getGivenName());
	}
}
