package io.github.pyvesb.alexaecopompe.geography;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.pyvesb.alexaecopompe.geography.CoordinateHelper;

class GeographicCoordinateHelperTest {

	@Test
	void shouldComputeLatitudeDifferenceForDistance() {
		assertEquals(0.089932031f, CoordinateHelper.computeLatDiff(10));
	}

	@Test
	void shouldComputeLongitudeDifferenceForDistanceAndLatitude() {
		assertEquals(0.1271831f, CoordinateHelper.computeLonDiff(10, 45.0));
	}

	@Test
	void shouldComputeSquaredDistanceBetweenTwoPositions() {
		assertEquals(6.33054542f, CoordinateHelper.computeSquaredDistance(45.0, 1.985, 45.0, 2.017));
		assertEquals(168.684997f, CoordinateHelper.computeSquaredDistance(44.32, 2.27, 44.26, 2.13));
		assertEquals(8793.724f, CoordinateHelper.computeSquaredDistance(43.87, 2.03, 44.712, 1.9637));
	}

}
