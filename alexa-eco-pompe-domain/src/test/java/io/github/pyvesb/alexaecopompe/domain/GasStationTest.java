package io.github.pyvesb.alexaecopompe.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class GasStationTest {

	@Test
	void shouldScaleDownLatitudeAndLongitudeIfWrongOrderOfMagnitude() {
		GasStation underTest = new GasStation("1", 4356100f, -107600f, "75001", "Paris", "rue Cler");

		assertEquals(43.561f, underTest.getLat());
		assertEquals(-1.076f, underTest.getLon());
	}

	@Test
	void shouldReturnPriceForGasType() {
		Price price1 = new Price(GasType.E85, LocalDate.of(2018, Month.APRIL, 4), 1.0f);
		Price price2 = new Price(GasType.SP98, LocalDate.of(2018, Month.APRIL, 5), 1.0f);
		GasStation underTest = new GasStation("1", 4.076f, 43.561f, "75001", "Paris", "rue Cler", price1, price2);

		Optional<Price> actualPrice1 = underTest.getPriceForGasType(GasType.E85);

		assertTrue(actualPrice1.isPresent());
		assertEquals(price1, actualPrice1.get());

		Optional<Price> actualPrice2 = underTest.getPriceForGasType(GasType.SP98);

		assertTrue(actualPrice2.isPresent());
		assertEquals(price2, actualPrice2.get());
	}

}
