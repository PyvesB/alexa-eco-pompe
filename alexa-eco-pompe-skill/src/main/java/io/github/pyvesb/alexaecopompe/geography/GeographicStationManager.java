package io.github.pyvesb.alexaecopompe.geography;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.pyvesb.alexaecopompe.domain.GasStation;

public class GeographicStationManager {

	private List<GasStation> gasStationsByLat = new ArrayList<>();
	private final List<GasStation> gasStationsByLon = new ArrayList<>();
	private boolean lonPreprocessRequired;

	public void registerGasStations(List<GasStation> gasStationsByLat) {
		this.gasStationsByLat = gasStationsByLat;
		// Lazily populate and sort the list by longitude.
		lonPreprocessRequired = true;
	}

	public List<GasStation> getGasStationsWithinRadius(Position position, int radius) {
		if (lonPreprocessRequired) {
			gasStationsByLon.clear();
			gasStationsByLon.addAll(gasStationsByLat);
			Collections.sort(gasStationsByLon, (gs1, gs2) -> Float.compare(gs1.getLon(), gs2.getLon()));
			lonPreprocessRequired = false;
		}
		return getGasStationsWithinRadius(position.getLat(), position.getLon(), radius);
	}

	private List<GasStation> getGasStationsWithinRadius(float lat, float lon, int radius) {
		float latDiff = CoordinateHelper.computeLatDiff(radius);
		List<GasStation> gasStationsInLatRange = getStationsInRange(lat, latDiff, gasStationsByLat, GasStation::getLat);
		float lonDiff = CoordinateHelper.computeLonDiff(radius, lat);
		List<GasStation> gasStationsInLonRange = getStationsInRange(lon, lonDiff, gasStationsByLon, GasStation::getLon);
		List<GasStation> commonGasStations = new ArrayList<>(gasStationsInLatRange);
		commonGasStations.retainAll(gasStationsInLonRange);
		int squaredRadius = radius * radius;
		return commonGasStations.stream()
				.filter(gs -> CoordinateHelper.computeSquaredDistance(gs.getLat(), gs.getLon(), lat, lon) <= squaredRadius)
				.collect(Collectors.toList());
	}

	private List<GasStation> getStationsInRange(float coordinate, float diff, List<GasStation> sortedGasStations,
			GasStationToFloatFunction coordinateGetter) {
		// Two range-based binary searches are performed. The first one operates on the full list, the second one starts
		// from the lower bound returned by the first one.
		int lowerBoundIndex = getSmallestIndexOverBound(coordinate - diff, sortedGasStations, coordinateGetter, -1);
		int upperBoundIndex = getSmallestIndexOverBound(coordinate + diff, sortedGasStations, coordinateGetter,
				lowerBoundIndex);
		// The upper index must be excluded as it's over the bound, which is consistent with sublist's behaviour.
		return sortedGasStations.subList(lowerBoundIndex, upperBoundIndex);
	}

	private int getSmallestIndexOverBound(float bound, List<GasStation> sortedGasStations,
			GasStationToFloatFunction coordinateGetter, int startIndex) {
		int middleIndex;
		int endIndex = sortedGasStations.size();
		while (endIndex - startIndex > 1) {
			middleIndex = (startIndex + endIndex) / 2;
			if (coordinateGetter.applyAsFloat(sortedGasStations.get(middleIndex)) > bound) {
				endIndex = middleIndex;
			} else {
				startIndex = middleIndex;
			}
		}
		return endIndex;
	}

	/**
	 * Used instead of ToDoubleFunction to avoid boxing/unboxing and narrowing/widening operations.
	 */
	@FunctionalInterface
	public interface GasStationToFloatFunction {

		float applyAsFloat(GasStation gs);
	}

}
