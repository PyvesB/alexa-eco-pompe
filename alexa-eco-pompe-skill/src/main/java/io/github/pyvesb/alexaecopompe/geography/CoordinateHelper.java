package io.github.pyvesb.alexaecopompe.geography;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

/**
 * This class is based on a spherical Earth projected to a plane approximation. See
 * https://en.wikipedia.org/wiki/Geographical_distance
 */
public class CoordinateHelper {

	private static final double DEGREES_TO_RADIANS = PI / 180;
	private static final double EARTH_RADIUS = 6371.009;
	private static final double SQUARED_EARTH_RADIUS = EARTH_RADIUS * EARTH_RADIUS;
	private static final double EARTH_RADIUS_RADIANS_TO_DEGREES = 180 / (EARTH_RADIUS * PI);

	public static float computeLatDiff(int distance) {
		return (float) (distance * EARTH_RADIUS_RADIANS_TO_DEGREES);
	}

	public static float computeLonDiff(int distance, double latitude) {
		return (float) (distance * EARTH_RADIUS_RADIANS_TO_DEGREES / cos(latitude * DEGREES_TO_RADIANS));
	}

	public static float computeSquaredDistance(double lat1, double lon1, double lat2, double lon2) {
		double latDiff = (lat1 - lat2) * DEGREES_TO_RADIANS;
		double lonDiff = (lon1 - lon2) * DEGREES_TO_RADIANS;
		double latMean = (lat1 + lat2) / 2 * DEGREES_TO_RADIANS;
		double cosMeanLonDiff = cos(latMean) * lonDiff;
		return (float) (SQUARED_EARTH_RADIUS * (latDiff * latDiff + cosMeanLonDiff * cosMeanLonDiff));
	}

	private CoordinateHelper() {
		// Not called.
	}

}
