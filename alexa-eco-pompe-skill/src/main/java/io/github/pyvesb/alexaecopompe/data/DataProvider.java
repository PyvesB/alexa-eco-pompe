package io.github.pyvesb.alexaecopompe.data;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.pyvesb.alexaecopompe.domain.GasStation;
import io.github.pyvesb.alexaecopompe.geography.GeographicStationManager;
import io.github.pyvesb.alexaecopompe.geography.Position;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class DataProvider {

	private static final int UNIQUE_POST_CODES = 4266;
	private static final int TOTAL_DEPARTMENTS = 96;
	private static final Logger LOGGER = LogManager.getLogger(DataProvider.class);
	private static final Schema<GasStation> SCHEMA = RuntimeSchema.getSchema(GasStation.class);

	private final Map<String, List<GasStation>> postCodesToGasStations = new HashMap<>(UNIQUE_POST_CODES);
	private final Map<String, List<GasStation>> departmentsToGasStations = new HashMap<>(TOTAL_DEPARTMENTS);
	private final String dataLocation;
	private final long stalenessThresholdMillis;
	private final Clock clock;
	private final GeographicStationManager geographicStationManager;

	private long dataAgeMillis;

	public DataProvider(String dataLocation, long stalenessThresholdMillis) {
		this(dataLocation, stalenessThresholdMillis, Clock.systemUTC(), new GeographicStationManager());
	}

	DataProvider(String dataLocation, long stalenessThresholdMillis, Clock clock,
			GeographicStationManager geographicStationManager) {
		this.dataLocation = dataLocation;
		this.stalenessThresholdMillis = stalenessThresholdMillis;
		this.clock = clock;
		this.geographicStationManager = geographicStationManager;
	}

	public List<GasStation> getGasStationsForPostCodes(String... postCodes) {
		fetchAndParseRawDataIfStale();
		return buildGasStationListForPostCodes(postCodes);
	}

	public List<GasStation> getGasStationsForDepartment(String departmentId) {
		fetchAndParseRawDataIfStale();
		return buildGasStationListForDepartment(departmentId);
	}

	public List<GasStation> getGasStationsWithinRadius(Position position, int radius) {
		fetchAndParseRawDataIfStale();
		return geographicStationManager.getGasStationsWithinRadius(position, radius);
	}

	private void fetchAndParseRawDataIfStale() {
		if (isStale()) {
			LOGGER.info("Retrieving gas station data");
			try (InputStream inputStream = new URL(dataLocation).openStream();
					InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream)) {
				parseDataStream(inflaterInputStream);
				dataAgeMillis = clock.millis();
			} catch (Exception e) {
				LOGGER.error("Exception when trying to retrieve and parse gas station data", e);
			}
		}
	}

	private boolean isStale() {
		return clock.millis() - dataAgeMillis >= stalenessThresholdMillis;
	}

	private void parseDataStream(InputStream inputStream) throws IOException {
		LOGGER.info("Parsing gas station data");
		// The input data must be sorted by latitude.
		List<GasStation> gasStationsSortedByLatitude = ProtostuffIOUtil.parseListFrom(inputStream, SCHEMA);
		if (!gasStationsSortedByLatitude.isEmpty()) {
			geographicStationManager.registerGasStations(gasStationsSortedByLatitude);
			// Keep the keys mapped to empty values to save on allocations when populating with fresh data.
			postCodesToGasStations.forEach((k, v) -> v.clear());
			departmentsToGasStations.forEach((k, v) -> v.clear());
			gasStationsSortedByLatitude.forEach(gs -> {
				postCodesToGasStations.computeIfAbsent(gs.getPostCode(), pc -> new ArrayList<>()).add(gs);
				departmentsToGasStations.computeIfAbsent(gs.getPostCode().substring(0, 2), pc -> new ArrayList<>()).add(gs);
			});
		}
	}

	private List<GasStation> buildGasStationListForPostCodes(String[] postCodes) {
		// If only one post code is requested, no need to allocate a new list.
		return postCodes.length == 1 ? postCodesToGasStations.getOrDefault(postCodes[0], emptyList())
				: Arrays.stream(postCodes)
						.flatMap(pc -> postCodesToGasStations.getOrDefault(pc, emptyList()).stream())
						.collect(Collectors.toList());
	}

	private List<GasStation> buildGasStationListForDepartment(String departmentId) {
		return departmentsToGasStations.getOrDefault(departmentId, emptyList());
	}

}
