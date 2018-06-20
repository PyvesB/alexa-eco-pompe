package io.github.pyvesb.alexaecopompe.json;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class APIFetcher<T> {

	private static final int TIMEOUT = 2500;
	private static final Logger LOGGER = LogManager.getLogger(APIFetcher.class);
	private static final ObjectReader READER = new ObjectMapper().reader();

	private final String baseUrl;
	private final Map<String, Optional<T>> resultCache = new HashMap<>();
	private final Function<JsonNode, T> extractor;

	public APIFetcher(String baseUrl, Function<JsonNode, T> extractor) {
		this.baseUrl = baseUrl;
		this.extractor = extractor;
	}

	public Optional<T> fetchForValue(String value) {
		Optional<T> result = resultCache.computeIfAbsent(value, v -> {
			try {
				URL url = new URL(String.format(baseUrl, URLEncoder.encode(v, UTF_8.name())));
				LOGGER.debug("Fetching result for url {}", url.toString());
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("Accept", "application/json");
				connection.setConnectTimeout(TIMEOUT);
				connection.setReadTimeout(TIMEOUT);
				return executeGet(connection);
			} catch (Exception e) {
				LOGGER.error("Exception whilst fetching result for value {}", v, e);
				return null; // Don't cache any value so that it can be retried later.
			}
		});
		return result == null ? Optional.empty() : result;
	}

	private Optional<T> executeGet(URLConnection connection) throws IOException {
		try (InputStream inputStream = connection.getInputStream()) {
			JsonNode jsonNode = READER.readTree(inputStream);
			return Optional.ofNullable(extractor.apply(jsonNode));
		}
	}

}
