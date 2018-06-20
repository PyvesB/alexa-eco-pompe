package io.github.pyvesb.alexaecopompe.json;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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

import io.github.pyvesb.alexaecopompe.json.APIFetcher;

class APIFetcherTest {

	private static final String API_PATH = "/path/to/api";

	private final WireMockServer wireMockServer = new WireMockServer(options().port(8089));

	private APIFetcher<String> underTest;

	@BeforeEach
	void setUp() {
		wireMockServer.start();
		WireMock.configureFor("localhost", wireMockServer.port());
		underTest = new APIFetcher<>("http://localhost:8089" + API_PATH + "?param=%s", jn -> "result");
	}

	@AfterEach
	void tearDown() {
		wireMockServer.stop();
	}

	@Test
	void shouldReturnResultForInputValue() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=75014008"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse().withBody("{}").withStatus(HTTP_OK)));

		Optional<String> result = underTest.fetchForValue("75014008");

		assertTrue(result.isPresent());
		assertEquals("result", result.get());
	}

	@Test
	void shouldReturnEmptyResultIfExceptionThown() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=75014008"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR)));

		Optional<String> result = underTest.fetchForValue("75014008");

		assertFalse(result.isPresent());
	}

	@Test
	void shouldReturnEmptyResultIfTimeout() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=75014008"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withBody("{}")
						.withStatus(HTTP_OK)
						.withFixedDelay(2500)));

		Optional<String> result = underTest.fetchForValue("75014008");

		assertFalse(result.isPresent());
	}

	@Test
	void shouldCacheResultProvidedNoExceptionThrown() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=75014008"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withBody("{}")
						.withStatus(HTTP_OK)));

		underTest.fetchForValue("75014008");
		underTest.fetchForValue("75014008");

		verify(exactly(1), getRequestedFor(anyUrl()));
	}

	@Test
	void shouldNotCacheEmptyResultIfExceptionThrown() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=75014008"))
				.withHeader("Accept", equalTo("application/json"))
				.inScenario("First failure")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR))
				.willSetStateTo("Second attempt"));

		wireMockServer.stubFor(get(urlEqualTo(API_PATH + "?param=75014008"))
				.withHeader("Accept", equalTo("application/json"))
				.inScenario("First failure")
				.whenScenarioStateIs("Second attempt")
				.willReturn(aResponse()
						.withBody("{}")
						.withStatus(HTTP_OK)));

		underTest.fetchForValue("75014008");
		Optional<String> result = underTest.fetchForValue("75014008");

		assertTrue(result.isPresent());
	}

}
