package io.github.pyvesb.alexaecopompe.address;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

class DeviceAddressProviderTest {

	private static final String DEVICE_ID = "abc123";
	private static final String API_TOKEN = "MQEWY6fnLok";
	private static final String API_PATH = "/v1/devices/" + DEVICE_ID + "/settings/address";

	private final WireMockServer wireMockServer = new WireMockServer(options().port(8089));

	private DeviceAddressProvider underTest;

	@BeforeEach
	void setUp() {
		wireMockServer.start();
		WireMock.configureFor("localhost", wireMockServer.port());
		underTest = new DeviceAddressProvider(500);
	}

	@AfterEach
	void tearDown() {
		wireMockServer.stop();
	}

	@Test
	void shouldReturnDeviceAddressGivenEndpointApiTokenAndDeviceId() throws Exception {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH))
				.withHeader("Authorization", equalTo("Bearer " + API_TOKEN))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(HTTP_OK)
						.withHeader("Content-Type", "application/json")
						.withBodyFile("address_response.json")));

		Address actualAddress = underTest.fetchAddress("http://localhost:8089", DEVICE_ID, API_TOKEN);
		Address expectedAddress = new Address("410 Terry Ave North", "Seattle", "98109");
		assertEquals(expectedAddress, actualAddress);
	}

	@Test
	void shouldThrowForbiddenExceptionIf403Returned() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH))
				.withHeader("Authorization", equalTo("Bearer " + API_TOKEN))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(HTTP_FORBIDDEN)));

		assertThrows(AddressForbiddenException.class,
				() -> underTest.fetchAddress("http://localhost:8089", DEVICE_ID, API_TOKEN));
	}
	
	@Test
	void shouldThrowNotSpecifiedExceptionIf204Returned() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH))
				.withHeader("Authorization", equalTo("Bearer " + API_TOKEN))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(HTTP_NO_CONTENT)));

		assertThrows(AddressNotSpecifiedException.class,
				() -> underTest.fetchAddress("http://localhost:8089", DEVICE_ID, API_TOKEN));
	}

	@Test
	void shouldThrowInaccessibleExceptionIfOtherErrorCodeReturned() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH))
				.withHeader("Authorization", equalTo("Bearer " + API_TOKEN))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(HTTP_INTERNAL_ERROR)));

		assertThrows(AddressInaccessibleException.class,
				() -> underTest.fetchAddress("http://localhost:8089", DEVICE_ID, API_TOKEN));
	}

	@Test
	void shouldThrowInaccessibleExceptionIfResponseCouldNotBeUnmarshalled() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH))
				.withHeader("Authorization", equalTo("Bearer " + API_TOKEN))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(HTTP_OK)
						.withHeader("Content-Type", "application/json")
						.withBody("")));

		assertThrows(AddressInaccessibleException.class,
				() -> underTest.fetchAddress("http://localhost:8089", DEVICE_ID, API_TOKEN));
	}
	
	@Test
	void shouldThrowInaccessibleExceptionIfTimeout() {
		wireMockServer.stubFor(get(urlEqualTo(API_PATH))
				.withHeader("Authorization", equalTo("Bearer " + API_TOKEN))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(HTTP_OK)
						.withHeader("Content-Type", "application/json")
						.withBodyFile("address_response.json")
						.withFixedDelay(500)));


		assertThrows(AddressInaccessibleException.class,
				() -> underTest.fetchAddress("http://localhost:8089", DEVICE_ID, API_TOKEN));
	}
	
}
