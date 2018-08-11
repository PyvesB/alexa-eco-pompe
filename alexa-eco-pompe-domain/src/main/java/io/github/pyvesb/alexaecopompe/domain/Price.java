package io.github.pyvesb.alexaecopompe.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class Price {

	private GasType type;
	private LocalDate updated;
	private float value;

	public Price() {}

	public Price(GasType type, LocalDate updated, float value) {
		this.type = type;
		this.updated = updated;
		this.value = value;
	}

	public GasType getType() {
		return type;
	}

	public LocalDate getUpdated() {
		return updated;
	}

	public float getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, updated, value);
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
		Price other = (Price) obj;
		return type == other.type && Objects.equals(updated, other.updated) && Float.compare(value, other.value) == 0;
	}

	@Override
	public String toString() {
		return "Price [type=" + type + ", updated=" + updated + ", value=" + value + "]";
	}

}
