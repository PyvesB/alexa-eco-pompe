package io.github.pyvesb.alexaecopompe.geography;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.github.pyvesb.alexaecopompe.address.Address;

public class PositionProvider {

	private static final Logger LOGGER = LogManager.getLogger(PositionProvider.class);
	private static final ObjectReader READER = new ObjectMapper().reader();

	private final Map<String, Optional<Position>> positionCache = new HashMap<>();
	private final String baseUrl;
	private final String userAgent;
	private final int timeout;
	private final JsonPointer latPointer;
	private final JsonPointer lonPointer;

	public PositionProvider(String baseUrl, String userAgent, int timeout, String latPath, String lonPath) {
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
		this.timeout = timeout;
		this.latPointer = JsonPointer.compile(latPath);
		this.lonPointer = JsonPointer.compile(lonPath);
	}

	public Optional<Position> getByAddress(Address address) {
		Optional<Position> position = getByAddress(address.toNormalisedString());
		// If not found, try simplified address with better chance of retrieving position. Less precise.
		return position.isPresent() ? position : getByAddress(address.toSimplifiedString());
	}

	private Optional<Position> getByAddress(String address) {
		Optional<Position> position = positionCache.computeIfAbsent(address, v -> {
			try {
				URL url = new URL(String.format(baseUrl, URLEncoder.encode(v, UTF_8.name())));
				LOGGER.debug("Fetching result for url {}", url.toString());
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("Accept", "application/json");
				connection.setRequestProperty("User-Agent", userAgent);
				connection.setConnectTimeout(timeout);
				connection.setReadTimeout(timeout);
				return executeGet(connection);
			} catch (Exception e) {
				LOGGER.error("Exception whilst fetching result for value {}", v, e);
				return null; // Don't cache any value so that it can be retried later.
			}
		});
		return position == null ? Optional.empty() : position;
	}

	private Optional<Position> executeGet(URLConnection connection) throws IOException {
		try (InputStream inputStream = connection.getInputStream()) {
			JsonNode jsonNode = READER.readTree(inputStream);
			JsonNode latNode = jsonNode.at(latPointer);
			JsonNode lonNode = jsonNode.at(lonPointer);
			boolean missing = latNode.isMissingNode() || lonNode.isMissingNode();
			return missing ? Optional.empty()
					: Optional.of(new Position(Float.parseFloat(latNode.asText()), Float.parseFloat(lonNode.asText())));
		}
	}

}
