package io.github.pyvesb.alexaecopompe.geography;

import java.util.Objects;

public class Position {

	private final float latitude;
	private final float longitude;

	public Position(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public float getLat() {
		return latitude;
	}

	public float getLon() {
		return longitude;
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitude, longitude);
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
		Position other = (Position) obj;
		return Float.compare(latitude, other.latitude) == 0 && Float.compare(longitude, other.longitude) == 0;
	}

}
