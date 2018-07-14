package io.github.pyvesb.alexaecopompe.geography;

import static java.lang.Float.compare;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.pyvesb.alexaecopompe.domain.GasStation;

class GeographicStationManagerTest {

	private final GeographicStationManager underTest = new GeographicStationManager();

	@Test
	void shouldComputeGasStationsWithinRadius() {
		GasStation gs1 = new GasStation("1", 4499000f, 201000f, "01000", "t", "a1"); // 1.362km from (45.0,2.0)
		GasStation gs2 = new GasStation("2", 4502698f, 200000f, "01000", "t", "a2"); // 3.0002km from (45.0,2.0)
		GasStation gs3 = new GasStation("3", 4503201f, 197800f, "01000", "t", "a3"); // 3.957km from (45.0,2.0)
		GasStation gs4 = new GasStation("4", 4503300f, 198000.2f, "01000", "t", "a4"); // 3.992km from (45.0,2.0)
		GasStation gs5 = new GasStation("5", 4500811f, 191180f, "01000", "t", "a5"); // 6.993km from (45.0,2.0)
		GasStation gs6 = new GasStation("6", 4490996f, 199999.6f, "01000", "t", "a6"); // 10.012km from (45.0,2.0)
		GasStation gs7 = new GasStation("7", 4490996f, 199999.6f, "01000", "t", "a7"); // 10.012km from (45.0,2.0)
		GasStation gs8 = new GasStation("8", 4488630f, 219000f, "01000", "t", "a8"); // 19.582km from (45.0,2.0)
		GasStation gs9 = new GasStation("9", 4500000f, 157100f, "01000", "t", "a9"); // 33.495km from (45.0,2.0)

		List<GasStation> gasStations = asList(gs8, gs6, gs7, gs1, gs9, gs5, gs2, gs3, gs4);
		assert gasStations.stream().sorted((s1, s2) -> compare(s1.getLat(), s2.getLat())).collect(toList())
				.equals(gasStations) : "The input test array should be sorted by latitude.";
		underTest.registerGasStations(gasStations);

		Position pos = new Position(45.0f, 2.0f);
		assertEquals(asList(), underTest.getGasStationsWithinRadius(pos, 0));
		assertEquals(asList(), underTest.getGasStationsWithinRadius(pos, 1));
		assertEquals(asList(gs1), underTest.getGasStationsWithinRadius(pos, 2));
		assertEquals(asList(gs1), underTest.getGasStationsWithinRadius(pos, 3));
		assertEquals(asList(gs1, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 4));
		assertEquals(asList(gs1, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 6));
		assertEquals(asList(gs1, gs5, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 7));
		assertEquals(asList(gs1, gs5, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 10));
		assertEquals(asList(gs6, gs7, gs1, gs5, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 11));
		assertEquals(asList(gs6, gs7, gs1, gs5, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 11));
		assertEquals(asList(gs6, gs7, gs1, gs5, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 19));
		assertEquals(asList(gs8, gs6, gs7, gs1, gs5, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 20));
		assertEquals(asList(gs8, gs6, gs7, gs1, gs5, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 33));
		assertEquals(asList(gs8, gs6, gs7, gs1, gs9, gs5, gs2, gs3, gs4), underTest.getGasStationsWithinRadius(pos, 34));
	}

	@Test
	void shouldCopeIfNoGasStationsAreRegistered() {
		assertEquals(asList(), underTest.getGasStationsWithinRadius(new Position(43.5f, 1.7f), 10));
	}

}
