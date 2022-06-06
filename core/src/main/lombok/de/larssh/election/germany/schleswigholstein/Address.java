package de.larssh.election.germany.schleswigholstein;

import java.util.Comparator;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Address
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Address implements Comparable<Address> {
	/**
	 * Comparator by ZIP, city, street and house number
	 */
	private static final Comparator<Address> COMPARATOR = Comparator.comparing(Address::getZip)
			.thenComparing(Address::getCity)
			.thenComparing(Address::getStreet)
			.thenComparing(Address::getHouseNumber);

	/**
	 * Street name without house number
	 *
	 * @return the street name without house number
	 */
	String street;

	/**
	 * House number
	 *
	 * @return the house number
	 */
	String houseNumber;

	/**
	 * Postal code
	 *
	 * @return the postal code
	 */
	String zip;

	/**
	 * City
	 *
	 * @return the city
	 */
	String city;

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nullable final Address address) {
		return COMPARATOR.compare(this, address);
	}
}
