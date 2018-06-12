package io.github.pyvesb.alexaecopompe.utils;

import static io.github.pyvesb.alexaecopompe.domain.GasType.E10;
import static io.github.pyvesb.alexaecopompe.domain.GasType.SP95;
import static io.github.pyvesb.alexaecopompe.domain.GasType.SP98;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.pyvesb.alexaecopompe.domain.GasStation;
import io.github.pyvesb.alexaecopompe.domain.Price;

class GasStationPriceSorterTest {

	private static final LocalDate OLD_DATE = LocalDate.of(2018, Month.APRIL, 2);
	private static final LocalDate RECENT_DATE = LocalDate.of(2018, Month.APRIL, 10);
	private GasStationPriceSorter underTest;

	@BeforeEach
	void setUp() {
		underTest = new GasStationPriceSorter(Clock.fixed(Instant.ofEpochMilli(1523318400000L), ZoneId.systemDefault()), 7L);
	}

	@Test
	void shouldSortGasStationList() {
		GasStation gs1 = new GasStation("1", 4.076f, 43.561f, "01000", "t", "a1", new Price(SP95, RECENT_DATE, 1.15f),
				new Price(SP98, RECENT_DATE, 1.18f));
		GasStation gs2 = new GasStation("2", 4.076f, 43.561f, "01000", "t", "a2", new Price(SP95, RECENT_DATE, 1.10f));
		GasStation gs3 = new GasStation("3", 4.076f, 43.561f, "01000", "t", "a3", new Price(SP95, RECENT_DATE, 1.20f),
				new Price(E10, RECENT_DATE, 1.18f));

		List<GasStation> gasStations = asList(gs1, gs2, gs3);

		underTest.sortGasStationsByIncreasingPricesForGasType(gasStations, SP95);
		assertEquals(asList(gs2, gs1, gs3), gasStations);

		underTest.sortGasStationsByIncreasingPricesForGasType(gasStations, SP98);
		assertEquals(gs1, gasStations.get(0));

		underTest.sortGasStationsByIncreasingPricesForGasType(gasStations, E10);
		assertEquals(gs3, gasStations.get(0));
	}

	@Test
	void shouldDegradeOrderingIfGasStationDataTooOld() {
		GasStation gs1 = new GasStation("1", 4.076f, 43.561f, "01000", "t", "a1", new Price(SP95, OLD_DATE, 1.15f),
				new Price(SP98, RECENT_DATE, 1.18f));
		GasStation gs2 = new GasStation("2", 4.076f, 43.561f, "01000", "t", "a2", new Price(SP95, OLD_DATE, 1.10f));
		GasStation gs3 = new GasStation("3", 4.076f, 43.561f, "01000", "t", "a3", new Price(SP95, RECENT_DATE, 1.20f),
				new Price(E10, RECENT_DATE, 1.18f));

		List<GasStation> gasStations = asList(gs1, gs2, gs3);

		underTest.sortGasStationsByIncreasingPricesForGasType(gasStations, SP95);
		assertEquals(asList(gs3, gs2, gs1), gasStations);
	}

}
