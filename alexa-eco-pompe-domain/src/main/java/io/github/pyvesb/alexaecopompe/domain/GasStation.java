package io.github.pyvesb.alexaecopompe.domain;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GasStation {

	private static final float COORDINATE_SCALE_DOWN_FACTOR = 100000f;

	private final String id;
	private final float latitude;
	private final float longitude;
	private final String postCode;
	private final String town;
	private final String address;
	private final List<Price> prices;

	public GasStation(String id, float latitude, float longitude, String postCode, String town, String address,
			Price... prices) {
		this(id, latitude, longitude, postCode, town, address, asList(prices));
	}

	public GasStation(String id, float latitude, float longitude, String postCode, String town, String address,
			List<Price> prices) {
		this.id = id;
		// The input data has both coordinate values scaled up by 100000.
		this.latitude = latitude / COORDINATE_SCALE_DOWN_FACTOR;
		this.longitude = longitude / COORDINATE_SCALE_DOWN_FACTOR;
		this.postCode = postCode;
		this.town = town;
		this.address = address;
		this.prices = prices;
	}

	public String getId() {
		return id;
	}

	public float getLat() {
		return latitude;
	}

	public float getLon() {
		return longitude;
	}

	public String getPostCode() {
		return postCode;
	}

	public String getTown() {
		return town;
	}

	public String getAddress() {
		return address;
	}

	public List<Price> getPrices() {
		return prices;
	}

	public Optional<Price> getPriceForGasType(GasType gasType) {
		return prices.stream().filter(p -> p.getType() == gasType).findAny();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, latitude, longitude, postCode, town, address, prices);
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
		GasStation other = (GasStation) obj;
		return Objects.equals(id, other.id) && Float.compare(latitude, other.latitude) == 0
				&& Float.compare(longitude, other.longitude) == 0 && Objects.equals(postCode, other.postCode)
				&& Objects.equals(town, other.town) && Objects.equals(address, other.address)
				&& Objects.equals(prices, other.prices);
	}

	@Override
	public String toString() {
		return "GasStation [id=" + id + ", latitude=" + latitude + ", longitude=" + longitude + ", postCode=" + postCode
				+ ", town=" + town + ", address=" + address + ", prices=" + prices + "]";
	}

}
