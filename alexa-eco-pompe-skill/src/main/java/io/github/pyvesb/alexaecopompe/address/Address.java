package io.github.pyvesb.alexaecopompe.address;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * Amazon seems to provide a similar class in the com.amazon.ask.model.services.deviceAddress package. Nevertheless, as
 * of yet the ask-sdk-model library is not documented anywhere and its source code is not released, which suggests that
 * it's not intended for public use at this point. This more lightweight class can be used for the time being.
 */
public class Address {

	private String addressLine1;
	private String city;
	private String postalCode;

	public Address() {}

	public Address(String addressLine1, String city, String postalCode) {
		this.addressLine1 = addressLine1;
		this.city = city;
		this.postalCode = postalCode;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public String getCity() {
		return city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(addressLine1, city, postalCode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Address other = (Address) obj;
		return Objects.equals(addressLine1, other.addressLine1) && Objects.equals(city, other.city)
				&& Objects.equals(postalCode, other.postalCode);
	}

	@Override
	public String toString() {
		// Simplified representation of the address that can easily be understood by geocoding APIs.
		return StringUtils.normalizeSpace(StringUtils.replaceAll(addressLine1, "\\d", "") + " " + city + " " + postalCode);
	}

}
