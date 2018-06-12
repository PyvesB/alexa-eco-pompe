package io.github.pyvesb.alexaecopompe.data;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.github.pyvesb.alexaecopompe.domain.GasType.E10;
import static io.github.pyvesb.alexaecopompe.domain.GasType.GAZOLE;
import static io.github.pyvesb.alexaecopompe.domain.GasType.SP95;
import static io.github.pyvesb.alexaecopompe.domain.GasType.SP98;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

import io.github.pyvesb.alexaecopompe.domain.GasStation;
import io.github.pyvesb.alexaecopompe.domain.Price;
import io.github.pyvesb.alexaecopompe.geography.GeographicStationManager;
import io.github.pyvesb.alexaecopompe.geography.Position;

@ExtendWith(MockitoExtension.class)
class DataProviderTest {

	private static final String DATA_PATH = "/path/to/data";
	private static final LocalDate DATE = LocalDate.of(2018, Month.APRIL, 4);
	private static final long INITIAL_TIME = 1526729962L;

	private final WireMockServer wireMockServer = new WireMockServer(options().port(8089));

	@Mock
	private GeographicStationManager geographicStationManager;

	@Mock
	private Clock clock;

	private DataProvider underTest;

	@BeforeEach
	void setUp() {
		wireMockServer.start();
		WireMock.configureFor("localhost", wireMockServer.port());
		when(clock.millis()).thenReturn(INITIAL_TIME);
		underTest = new DataProvider("http://localhost:8089" + DATA_PATH, 1000L, clock, geographicStationManager);
	}

	@AfterEach
	void tearDown() {
		wireMockServer.stop();
	}

	@Nested
	class ValidResponses {

		@BeforeEach
		void setUp() {
			wireMockServer.stubFor(get(urlEqualTo(DATA_PATH))
					.willReturn(aResponse()
							.withStatus(HTTP_OK)
							.withHeader("Content-Type", "application/octet-stream")
							.withBodyFile("gas-station.data")));
		}

		@Test
		void shouldReturnGasStationsForPostCodes() {
			GasStation gs1 = new GasStation("1", 43.560f, 4.075f, "75001", "Parîs", "Place Vendôme",
					new Price(GAZOLE, DATE, 1.336f));
			GasStation gs2 = new GasStation("2", 43.561f, 4.076f, "75014", "paris", "Montparnasse",
					new Price(SP95, DATE, 1.266f), new Price(SP98, DATE, 1.442f));
			GasStation gs3 = new GasStation("3", 43.562f, 4.077f, "73100", "AIX LES BAINS", "A41",
					new Price(E10, DATE, 1.399f));

			List<GasStation> gasStationsInParis = underTest.getGasStationsForPostCodes("75001", "75014");
			assertEquals(asList(gs1, gs2), gasStationsInParis);

			List<GasStation> gasStationsInAix = underTest.getGasStationsForPostCodes("73100");
			assertEquals(asList(gs3), gasStationsInAix);
		}

		@Test
		void shouldReturnEmptyListIfNoGasStationsFoundForPostCode() {
			List<GasStation> gasStationsInBled = underTest.getGasStationsForPostCodes("58400");
			assertEquals(emptyList(), gasStationsInBled);
		}

		@Test
		void shouldReturnGasStationsForDepartmentIdentifier() {
			GasStation gs1 = new GasStation("1", 43.560f, 4.075f, "75001", "Parîs", "Place Vendôme",
					new Price(GAZOLE, DATE, 1.336f));
			GasStation gs2 = new GasStation("2", 43.561f, 4.076f, "75014", "paris", "Montparnasse",
					new Price(SP95, DATE, 1.266f), new Price(SP98, DATE, 1.442f));
			GasStation gs3 = new GasStation("3", 43.562f, 4.077f, "73100", "AIX LES BAINS", "A41",
					new Price(E10, DATE, 1.399f));

			List<GasStation> gasStationsInParis = underTest.getGasStationsForDepartment("75");
			assertEquals(asList(gs1, gs2), gasStationsInParis);

			List<GasStation> gasStationsInSavoie = underTest.getGasStationsForDepartment("73");
			assertEquals(asList(gs3), gasStationsInSavoie);
		}

		@Test
		void shouldReturnEmptyListIfNoGasStationsFoundForDepartment() {
			List<GasStation> gasStationsInLaReunion = underTest.getGasStationsForDepartment("97");
			assertEquals(emptyList(), gasStationsInLaReunion);
		}

		@Test
		void shouldReturnGasStationsForPositionAndRadius() {
			GasStation gs1 = new GasStation("1", 43.560f, 4.075f, "75001", "Parîs", "Place Vendôme",
					new Price(GAZOLE, DATE, 1.336f));
			GasStation gs2 = new GasStation("2", 43.561f, 4.076f, "75014", "paris", "Montparnasse",
					new Price(SP95, DATE, 1.266f), new Price(SP98, DATE, 1.442f));
			GasStation gs3 = new GasStation("3", 43.562f, 4.077f, "73100", "AIX LES BAINS", "A41",
					new Price(E10, DATE, 1.399f));
			when(geographicStationManager.getGasStationsWithinRadius(any(), anyInt())).thenReturn(asList(gs1));

			Position position = new Position(43.560f, 4.075f);
			List<GasStation> gasStationsNearParis = underTest.getGasStationsWithinRadius(position, 10);
			assertEquals(asList(gs1), gasStationsNearParis);
			verify(geographicStationManager).registerGasStations(asList(gs1, gs2, gs3));
			verify(geographicStationManager).getGasStationsWithinRadius(position, 10);
		}

		@Test
		void shouldOnlyFetchDataAgainIfItBecomesStale() {
			underTest.getGasStationsForPostCodes("75001,75014");
			when(clock.millis()).thenReturn(INITIAL_TIME + 999L);
			underTest.getGasStationsForPostCodes("58400");

			verify(exactly(1), getRequestedFor(anyUrl()));

			when(clock.millis()).thenReturn(INITIAL_TIME + 1000L);
			underTest.getGasStationsForPostCodes("06130");

			verify(exactly(2), getRequestedFor(anyUrl()));
		}

	}

	@Nested
	class InvalidResponses {

		@Test
		void shouldNotDiscardStaleDataIfFreshDataCouldNotBeRetrieved() {
			wireMockServer.stubFor(get(urlEqualTo(DATA_PATH))
					.inScenario("Failure on second query")
					.whenScenarioStateIs(Scenario.STARTED)
					.willReturn(aResponse()
							.withStatus(HTTP_OK)
							.withHeader("Content-Type", "application/octet-stream")
							.withBodyFile("gas-station.data"))
					.willSetStateTo("First retrieval"));

			wireMockServer.stubFor(get(urlEqualTo(DATA_PATH))
					.inScenario("Failure on second query")
					.whenScenarioStateIs("First retrieval")
					.willReturn(aResponse()
							.withStatus(HTTP_INTERNAL_ERROR)));

			underTest.getGasStationsForPostCodes("75001,75014");
			when(clock.millis()).thenReturn(INITIAL_TIME + 2000L);
			List<GasStation> gasStationsInAix = underTest.getGasStationsForPostCodes("73100");
			GasStation gs = new GasStation("3", 43.562f, 4.077f, "73100", "AIX LES BAINS", "A41",
					new Price(E10, DATE, 1.399f));
			assertEquals(asList(gs), gasStationsInAix);
		}

		@Test
		void shouldReturnEmptyListIfParsingReturnsNoGasStations() {
			wireMockServer.stubFor(get(urlEqualTo(DATA_PATH))
					.willReturn(aResponse()
							.withStatus(HTTP_OK)
							.withHeader("Content-Type", "application/octet-stream")));

			underTest.getGasStationsForPostCodes("75001,75014");
			List<GasStation> gasStationsInAix = underTest.getGasStationsForPostCodes("73100");
			assertEquals(emptyList(), gasStationsInAix);
		}
	}

}
