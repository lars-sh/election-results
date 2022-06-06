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

	@EqualsAndHashCode.Include
	String givenName;

	@EqualsAndHashCode.Include
	String familyName;

	Optional<Gender> gender;

	OptionalInt yearOfBirth;

	Optional<Locale> nationality;

	Optional<Address> address;

	Optional<String> job;

	@Override
	public int compareTo(@Nullable final Person party) {
		return COMPARATOR.compare(this, party);
	}

	@JsonIgnore
	public String getKey() {
		return createKey(getFamilyName(), getGivenName());
	}
}
