package io.github.pyvesb.alexaecopompe.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.pyvesb.alexaecopompe.geography.Position;

class JsonExtractorsTest {

	@Test
	void shouldExtractName() throws Exception {
		Function<JsonNode, String> underTest = JsonExtractors.nameExtractor("/records/0/fields/name");

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("name_api_response.json");
		JsonNode jsonNode = new ObjectMapper().readTree(inputStream);
		String result = underTest.apply(jsonNode);
		assertEquals("Sarl Pyves Gas", result);
	}

	@Test
	void shouldReturnNullIfNameCouldNotBeFound() throws Exception {
		Function<JsonNode, String> underTest = JsonExtractors.nameExtractor("/records/0/fields/name");

		InputStream inputStream = IOUtils.toInputStream("{}", StandardCharsets.UTF_8);
		JsonNode jsonNode = new ObjectMapper().readTree(inputStream);
		assertNull(underTest.apply(jsonNode));
	}

	@Test
	void shouldExtractPosition() throws Exception {
		Function<JsonNode, Position> underTest = JsonExtractors.positionExtractor("/0/lat", "/0/lon");

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("address_api_response.json");
		JsonNode jsonNode = new ObjectMapper().readTree(inputStream);
		Position result = underTest.apply(jsonNode);
		assertEquals(new Position(48.8674634f, 2.32942811682519f), result);
	}

	@Test
	void shouldReturnNullIfPositionDataIsInvalid() throws Exception {
		Function<JsonNode, Position> underTest = JsonExtractors.positionExtractor("/0/lat", "/0/lon");

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("address_api_invalid_response.json");
		JsonNode jsonNode = new ObjectMapper().readTree(inputStream);
		assertNull(underTest.apply(jsonNode));
	}

	@Test
	void shouldReturnNullIfPositionDataCouldNotBeFound() throws Exception {
		Function<JsonNode, Position> underTest = JsonExtractors.positionExtractor("/0/lat", "/0/lon");

		InputStream inputStream = IOUtils.toInputStream("[]", StandardCharsets.UTF_8);
		JsonNode jsonNode = new ObjectMapper().readTree(inputStream);
		assertNull(underTest.apply(jsonNode));
	}

}
