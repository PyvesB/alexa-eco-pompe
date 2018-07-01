package io.github.pyvesb.alexaecopompe.geography;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.github.pyvesb.alexaecopompe.address.Address;
import io.github.pyvesb.alexaecopompe.geography.Position;
import io.github.pyvesb.alexaecopompe.geography.PositionProvider;

class PositionProviderTest {

	private static final String API_PATH = "/path/to/api";
	private static final Address ADDRESS = new Address("1 rue Cler", "Paris", "75001");

	private final WireMockServer wireMockServer = new WireMockServer(options().port(8089));

	private PositionProvider underTest;

	@BeforeEach
	void setUp() {
		wireMockServer.start();
		WireMock.configureFor("localhost", wireMockServer.port());
		underTest = new PositionProvider("http://localhost:8089" + API_PATH + "?param=%s", "user", 500, "/0/lat", "/0/lon");
	}

	@AfterEach
	void tearDown() {
		wireMockServer.stop();
	}

	@Test
	void shouldReturnPositionForAddress() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=rue+Cler+Paris+75001"))
				.withHeader("Accept", equalTo("application/json"))
				.withHeader("User-Agent", equalTo("user"))
				.willReturn(aResponse()
						.withBodyFile("position_response.json")
						.withStatus(HTTP_OK)));

		Optional<Position> position = underTest.getByAddress(ADDRESS);

		assertTrue(position.isPresent());
		assertEquals(new Position(48.8674634f, 2.32942811682519f), position.get());
	}

	@Test
	void shouldReturnPositionForAddressUsingSimplifiedAddress() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=rue+Cler+Paris+75001"))
				.withHeader("Accept", equalTo("application/json"))
				.withHeader("User-Agent", equalTo("user"))
				.willReturn(aResponse()
						.withBody("[]")
						.withStatus(HTTP_OK)));

		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=Paris+75001"))
				.withHeader("Accept", equalTo("application/json"))
				.withHeader("User-Agent", equalTo("user"))
				.willReturn(aResponse()
						.withBodyFile("position_response.json")
						.withStatus(HTTP_OK)));

		Optional<Position> position = underTest.getByAddress(ADDRESS);

		assertTrue(position.isPresent());
		assertEquals(new Position(48.8674634f, 2.32942811682519f), position.get());
	}

	@Test
	void shouldReturnEmptyResultIfResponseMissingLatitudeOrLongitude() {
		wireMockServer.stubFor(get(urlMatching(API_PATH + "\\?param=.*$"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withBody("[{\"lat\": \"1.0\"}]")
						.withStatus(HTTP_OK)));

		Optional<Position> position = underTest.getByAddress(ADDRESS);

		assertFalse(position.isPresent());
	}

	@Test
	void shouldReturnEmptyResultIfResponseInvalid() {
		wireMockServer.stubFor(get(urlMatching(API_PATH + "\\?param=.*$"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(HTTP_INTERNAL_ERROR)));

		Optional<Position> position = underTest.getByAddress(ADDRESS);

		assertFalse(position.isPresent());
	}

	@Test
	void shouldReturnEmptyResultIfTimeouts() {
		wireMockServer.stubFor(get(urlMatching(API_PATH + "\\?param=.*$"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withBodyFile("position_response.json")
						.withStatus(HTTP_OK)
						.withFixedDelay(500)));

		Optional<Position> position = underTest.getByAddress(ADDRESS);

		assertFalse(position.isPresent());
	}

	@Test
	void shouldCacheResultIfValidPosition() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=rue+Cler+Paris+75001"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withBodyFile("position_response.json")
						.withStatus(HTTP_OK)));

		underTest.getByAddress(ADDRESS);
		underTest.getByAddress(ADDRESS);

		verify(exactly(1), getRequestedFor(anyUrl()));
	}

	@Test
	void shouldCacheResultIfResponseMissingLatitudeOrLongitude() {
		wireMockServer.stubFor(get(urlMatching(API_PATH + "\\?param=.*$"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withBody("[]")
						.withStatus(HTTP_OK)));

		underTest.getByAddress(ADDRESS);
		underTest.getByAddress(ADDRESS);

		// Expect 2 as simplified address will be used as an alternative.
		verify(exactly(2), getRequestedFor(anyUrl()));
	}

	@Test
	void shouldNotCacheEmptyResultIfResponseInvalid() {
		wireMockServer.stubFor(get(urlMatching(API_PATH + "\\?param=.*$"))
				.withHeader("Accept", equalTo("application/json"))
				.inScenario("First failure")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR))
				.willSetStateTo("Second attempt"));

		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=rue+Cler+Paris+75001"))
				.withHeader("Accept", equalTo("application/json"))
				.inScenario("First failure")
				.whenScenarioStateIs("Second attempt")
				.willReturn(aResponse()
						.withBodyFile("position_response.json")
						.withStatus(HTTP_OK)));

		underTest.getByAddress(ADDRESS);
		Optional<Position> position = underTest.getByAddress(ADDRESS);

		assertTrue(position.isPresent());
	}

	@Test
	void shouldNotCacheEmptyResultIfTimeouts() {
		wireMockServer.stubFor(get(urlMatching(API_PATH + "\\?param=.*$"))
				.withHeader("Accept", equalTo("application/json"))
				.inScenario("First failure")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withBodyFile("position_response.json")
						.withStatus(HTTP_OK)
						.withFixedDelay(500))
				.willSetStateTo("Second attempt"));

		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=rue+Cler+Paris+75001"))
				.withHeader("Accept", equalTo("application/json"))
				.inScenario("First failure")
				.whenScenarioStateIs("Second attempt")
				.willReturn(aResponse()
						.withBodyFile("position_response.json")
						.withStatus(HTTP_OK)));

		underTest.getByAddress(ADDRESS);
		Optional<Position> position = underTest.getByAddress(ADDRESS);

		assertTrue(position.isPresent());
	}

}
