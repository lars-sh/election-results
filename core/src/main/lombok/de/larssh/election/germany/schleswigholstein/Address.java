package de.larssh.election.germany.schleswigholstein;

import java.util.Comparator;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Address implements Comparable<Address> {
	private static final Comparator<Address> COMPARATOR = Comparator.comparing(Address::getZip)
			.thenComparing(Address::getCity)
			.thenComparing(Address::getStreet)
			.thenComparing(Address::getHouseNumber);

	String street;

	String houseNumber;

	String zip;

	String city;

	@Override
	public int compareTo(@Nullable final Address address) {
		return COMPARATOR.compare(this, address);
	}
}
