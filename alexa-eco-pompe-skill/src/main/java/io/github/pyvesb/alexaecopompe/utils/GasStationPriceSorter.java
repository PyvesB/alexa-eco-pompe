package io.github.pyvesb.alexaecopompe.utils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import io.github.pyvesb.alexaecopompe.domain.GasStation;
import io.github.pyvesb.alexaecopompe.domain.GasType;
import io.github.pyvesb.alexaecopompe.domain.Price;

public class GasStationPriceSorter {

	// Arbitrary value to relatively degrade the ordering of prices is they are outdated.
	private static final float STALE_PRICE_PENALTY = 1000f;

	private final Clock clock;
	private final long priceStalenessThresholdDays;

	public GasStationPriceSorter(long priceStalenessThresholdDays) {
		this(Clock.systemUTC(), priceStalenessThresholdDays);
	}

	GasStationPriceSorter(Clock clock, long priceStalenessThresholdDays) {
		this.clock = clock;
		this.priceStalenessThresholdDays = priceStalenessThresholdDays;
	}

	public void sortGasStationsByIncreasingPricesForGasType(List<GasStation> gasStations, GasType gasType) {
		Collections.sort(gasStations, (gs1, gs2) -> compareGasStationsForGasType(gs1, gs2, gasType));
	}

	private int compareGasStationsForGasType(GasStation gs1, GasStation gs2, GasType gasType) {
		float price1 = getPriceForGasType(gs1, gasType);
		float price2 = getPriceForGasType(gs2, gasType);
		return Float.compare(price1, price2);
	}

	private float getPriceForGasType(GasStation gs, GasType gasType) {
		return gs.getPriceForGasType(gasType)
				.map(p -> isPriceStale(p) ? p.getValue() + STALE_PRICE_PENALTY : p.getValue())
				.orElse(Float.MAX_VALUE);
	}

	private boolean isPriceStale(Price price) {
		return LocalDate.now(clock).minus(priceStalenessThresholdDays, ChronoUnit.DAYS).isAfter(price.getUpdated());
	}

}
