package io.github.pyvesb.alexaecopompe.data;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.github.pyvesb.alexaecopompe.domain.GasType.E10;
import static io.github.pyvesb.alexaecopompe.domain.GasType.E85;
import static io.github.pyvesb.alexaecopompe.domain.GasType.GAZOLE;
import static io.github.pyvesb.alexaecopompe.domain.GasType.SP98;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.zip.InflaterInputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.github.tomakehurst.wiremock.WireMockServer;

import io.github.pyvesb.alexaecopompe.domain.GasStation;
import io.github.pyvesb.alexaecopompe.domain.Price;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

@ExtendWith(MockitoExtension.class)
class DataPreProcessorTest {

	private static final String DATA_PATH = "/path/to/data";
	private static final LocalDate DATE1 = LocalDate.of(2018, Month.MARCH, 22);
	private static final LocalDate DATE2 = LocalDate.of(2018, Month.MARCH, 23);

	private final WireMockServer wireMockServer = new WireMockServer(options().port(8089));

	@Mock
	private AmazonS3 amazonS3;

	@Captor
	public ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor;

	private DataPreProcessor underTest;

	@BeforeEach
	void setUp() {
		wireMockServer.start();
		underTest = new DataPreProcessor(amazonS3, "http://localhost:8089" + DATA_PATH);
	}

	@AfterEach
	void tearDown() {
		wireMockServer.stop();
	}

	@Test
	void shouldFetchUnzipSortAndUploadSerialisedGazStationDataToS3() throws Exception {
		wireMockServer.stubFor(get(urlEqualTo(DATA_PATH))
				.willReturn(aResponse()
						.withStatus(HTTP_OK)
						.withHeader("Content-Type", "application/octet-stream")
						.withBodyFile("PrixCarburants_instantane.zip")));

		underTest.handleRequest(null, null);

		verify(amazonS3).putObject(putObjectRequestCaptor.capture());
		PutObjectRequest putObjectRequest = putObjectRequestCaptor.getValue();

		assertEquals("alexa-eco-pompe", putObjectRequest.getBucketName());
		assertEquals("gas-station.data", putObjectRequest.getKey());
		assertEquals(CannedAccessControlList.PublicRead, putObjectRequest.getCannedAcl());
		assertEquals(212L, putObjectRequest.getMetadata().getContentLength());

		GasStation gs1 = new GasStation("7340002", 4525743f, 469631.2f, "07430", "Davézieux", "LE MAS OUEST - RTE DE LYON",
				new Price(GAZOLE, DATE2, 1.389f), new Price(E85, DATE2, 0.749f), new Price(E10, DATE2, 1.448f));
		GasStation gs2 = new GasStation("94470005", 4875500f, 250500f, "94470", "Boissy-Saint-Léger",
				"Avenue du Général Leclerc", new Price(GAZOLE, DATE1, 1.439f), new Price(E10, DATE1, 1.549f),
				new Price(SP98, DATE1, 1.599f));
		try (InflaterInputStream inflaterInputStream = new InflaterInputStream(putObjectRequest.getInputStream())) {
			List<GasStation> actualGasStations = ProtostuffIOUtil.parseListFrom(inflaterInputStream,
					RuntimeSchema.getSchema(GasStation.class));
			assertEquals(asList(gs1, gs2), actualGasStations);
		}
		verifyNoMoreInteractions(amazonS3);
	}

	@Test
	void shouldFilterOutGasStationsWithoutAnyPrices() throws Exception {
		wireMockServer.stubFor(get(urlEqualTo(DATA_PATH))
				.willReturn(aResponse()
						.withStatus(HTTP_OK)
						.withHeader("Content-Type", "application/octet-stream")
						.withBodyFile("PrixCarburants_instantane_no_prices.zip")));

		underTest.handleRequest(null, null);

		verify(amazonS3).putObject(putObjectRequestCaptor.capture());
		PutObjectRequest putObjectRequest = putObjectRequestCaptor.getValue();
		 assertEquals(8L, putObjectRequest.getMetadata().getContentLength());

		try (InflaterInputStream inflaterInputStream = new InflaterInputStream(putObjectRequest.getInputStream())) {
			List<GasStation> actualGasStations = ProtostuffIOUtil.parseListFrom(inflaterInputStream,
					RuntimeSchema.getSchema(GasStation.class));
			assertEquals(emptyList(), actualGasStations);
		}
	}

	@Test
	void shouldRethrowRuntimeExceptionIfIOExceptionIsCaught() {
		wireMockServer.stubFor(get(urlEqualTo(DATA_PATH)).willReturn(aResponse().withStatus(HTTP_INTERNAL_ERROR)));

		assertThrows(RuntimeException.class, () -> underTest.handleRequest(null, null));
		verifyZeroInteractions(amazonS3);
	}

}
