package io.github.pyvesb.alexaecopompe.handlers;

import static io.github.pyvesb.alexaecopompe.domain.GasType.E10;
import static io.github.pyvesb.alexaecopompe.domain.GasType.GAZOLE;
import static io.github.pyvesb.alexaecopompe.domain.GasType.SP95;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.InputBuilder.API_ACCESS_TOKEN;
import static utils.InputBuilder.API_ENDPOINT;
import static utils.InputBuilder.DEVICE_ID;
import static utils.InputBuilder.buildDepartmentInput;
import static utils.InputBuilder.buildIntentInput;
import static utils.InputBuilder.buildIntentInputWithNoGasValue;
import static utils.InputBuilder.buildLaunchInput;
import static utils.InputBuilder.buildNearbyInput;
import static utils.InputBuilder.buildRadiusInput;
import static utils.InputBuilder.buildTownInput;
import static utils.ResponseAssertions.assertCard;
import static utils.ResponseAssertions.assertCardWithPermissions;
import static utils.ResponseAssertions.assertSpeech;
import static utils.TestFactoryHelper.buildDynamicDisplayName;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;

import io.github.pyvesb.alexaecopompe.address.Address;
import io.github.pyvesb.alexaecopompe.address.AddressForbiddenException;
import io.github.pyvesb.alexaecopompe.address.AddressInaccessibleException;
import io.github.pyvesb.alexaecopompe.address.DeviceAddressProvider;
import io.github.pyvesb.alexaecopompe.data.DataProvider;
import io.github.pyvesb.alexaecopompe.data.NameProvider;
import io.github.pyvesb.alexaecopompe.domain.GasStation;
import io.github.pyvesb.alexaecopompe.domain.GasType;
import io.github.pyvesb.alexaecopompe.domain.Price;
import io.github.pyvesb.alexaecopompe.geography.Position;
import io.github.pyvesb.alexaecopompe.geography.PositionProvider;
import io.github.pyvesb.alexaecopompe.utils.GasStationPriceSorter;
import utils.MissingResponse;

@ExtendWith(MockitoExtension.class)
class MainIntentHandlerTest {

	private static final LocalDate DATE = LocalDate.of(2018, Month.APRIL, 4);
	private static final LocalDate TODAY = LocalDate.now();
	private static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);

	@Mock
	private DataProvider dataProvider;
	@Mock
	private NameProvider nameProvider;
	@Mock
	private PositionProvider positionProvider;
	@Mock
	private DeviceAddressProvider deviceAddressProvider;
	@Mock
	private GasStationPriceSorter gasStationPriceSorter;

	private MainIntentHandler underTest;

	@BeforeEach
	void setUp() {
		underTest = new MainIntentHandler(dataProvider, nameProvider, positionProvider, deviceAddressProvider,
				gasStationPriceSorter);
	}

	@ParameterizedTest
	@ValueSource(strings = { "GasRadius", "SomeIntentName" })
	void shouldHandleIntentRequestsWithGasIntentNameOrOtherIntentName(String intentName) {
		assertTrue(underTest.canHandle(buildIntentInput(intentName)));
	}

	@Test
	void shouldNotHandleOtherRequests() {
		assertFalse(underTest.canHandle(buildLaunchInput()));
	}

	@Test
	@Tags({ @Tag("happy"), @Tag("department") })
	void shouldReturnPriceOfCheapestGasStationForRequestedDepartmentAndGasType() {
		GasStation gs1 = new GasStation("1", 43.5f, 4.0f, "73000", "Chambéry", "rue Favre", new Price(GAZOLE, DATE, 1.10f));
		GasStation gs2 = new GasStation("2", 43.6f, 4.0f, "73000", "Chambéry", "rue Juiverie", new Price(SP95, DATE, 1.15f));
		when(dataProvider.getGasStationsForDepartment(any())).thenReturn(asList(gs1, gs2));
		when(nameProvider.getById(any())).thenReturn(Optional.of("Pyves Gas"));

		Response resp = underTest.handle(buildDepartmentInput(GAZOLE, "Savoie", "73")).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertCard("Pyves Gas\nRue Favre, Chambéry\nGazole : 1€10", resp);
		assertSpeech("Pyves Gas vend du gazole pour 1€10. Cette pompe est située Rue Favre à Chambéry, et a actualisé ses "
				+ "tarifs le 2018-04-04.", resp);
		verify(dataProvider).getGasStationsForDepartment("73");
		verify(gasStationPriceSorter).sortGasStationsByIncreasingPricesForGasType(asList(gs1, gs2), GAZOLE);
		verify(nameProvider).getById("1");
	}

	@Test
	@Tags({ @Tag("happy"), @Tag("radius") })
	void shouldReturnPriceOfCheapestGasStationForRequestedRadiusAndGasType() throws Exception {
		Address address = new Address("54Bis rue Cler", "Paris", "75002");
		when(deviceAddressProvider.fetchAddress(any(), any(), any())).thenReturn(address);
		Position position = new Position(43.6f, 4.08f);
		when(positionProvider.getByAddress(any())).thenReturn(Optional.of(position));
		GasStation gs = new GasStation("1", 43.561f, 4.076f, "75002", "Paris", "rue Cler", new Price(SP95, TODAY, 1.10f));
		when(dataProvider.getGasStationsWithinRadius(any(), anyInt())).thenReturn(asList(gs));
		when(nameProvider.getById(any())).thenReturn(Optional.of("Pyves Gas"));

		Response resp = underTest.handle(buildRadiusInput(SP95, "10", true)).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertCard("Pyves Gas\nRue Cler, Paris\nSans Plomb 95 : 1€10", resp);
		assertSpeech("Pyves Gas vend du sans plomb 95 pour 1€10. Cette pompe est située Rue Cler à Paris, et a actualisé "
				+ "ses tarifs aujourd'hui.", resp);
		verify(deviceAddressProvider).fetchAddress(API_ENDPOINT, DEVICE_ID, API_ACCESS_TOKEN);
		verify(positionProvider).getByAddress(address);
		verify(dataProvider).getGasStationsWithinRadius(position, 10);
		verify(gasStationPriceSorter).sortGasStationsByIncreasingPricesForGasType(asList(gs), SP95);
		verify(nameProvider).getById("1");
	}

	@Test
	@Tags({ @Tag("happy"), @Tag("nearby") })
	void shouldReturnPriceOfCheapestGasStationNearbyForRequestedGasType() throws Exception {
		Address address = new Address("54 rue Cler", null, "75002");
		when(deviceAddressProvider.fetchAddress(any(), any(), any())).thenReturn(address);
		Position position = new Position(43.6f, 4.08f);
		when(positionProvider.getByAddress(any())).thenReturn(Optional.of(position));
		GasStation gs = new GasStation("1", 43.561f, 4.076f, "75002", "Paris", "rue Cler", new Price(SP95, TODAY, 1.10f));
		when(dataProvider.getGasStationsWithinRadius(any(), anyInt())).thenReturn(asList(gs));
		when(nameProvider.getById(any())).thenReturn(Optional.of("Pyves Gas"));

		Response resp = underTest.handle(buildNearbyInput(SP95, true)).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertCard("Pyves Gas\nRue Cler, Paris\nSans Plomb 95 : 1€10", resp);
		assertSpeech("Pyves Gas vend du sans plomb 95 pour 1€10. Cette pompe est située Rue Cler à Paris, et a actualisé "
				+ "ses tarifs aujourd'hui.", resp);
		verify(deviceAddressProvider).fetchAddress(API_ENDPOINT, DEVICE_ID, API_ACCESS_TOKEN);
		verify(positionProvider).getByAddress(address);
		verify(dataProvider).getGasStationsWithinRadius(position, 5);
		verify(gasStationPriceSorter).sortGasStationsByIncreasingPricesForGasType(asList(gs), SP95);
		verify(nameProvider).getById("1");
	}

	@Test
	@Tags({ @Tag("happy"), @Tag("town") })
	void shouldReturnPriceOfCheapestGasStationForRequestedTownAndGasType() {
		GasStation gs = new GasStation("1", 43.561f, 4.076f, "75002", "Paris", "rue Cler", new Price(SP95, TODAY, 1.10f));
		when(dataProvider.getGasStationsForPostCodes(any())).thenReturn(asList(gs));
		when(nameProvider.getById(any())).thenReturn(Optional.of("Pyves Gas"));

		Response resp = underTest.handle(buildTownInput(SP95, "Paris", "c05,75001,75002")).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertCard("Pyves Gas\nRue Cler, Paris\nSans Plomb 95 : 1€10", resp);
		assertSpeech("Pyves Gas vend du sans plomb 95 pour 1€10. Cette pompe est située Rue Cler à Paris, et a actualisé "
				+ "ses tarifs aujourd'hui.", resp);
		verify(dataProvider).getGasStationsForPostCodes("75001", "75002");
		verify(gasStationPriceSorter).sortGasStationsByIncreasingPricesForGasType(asList(gs), SP95);
		verify(nameProvider).getById("1");
	}

	@Test
	@Tags({ @Tag("happy"), @Tag("town") })
	void shouldReturnPriceOfCheapestGasStationForRequestedTownAndGasTypeIfGasStationNameIsMissing() {
		GasStation gs = new GasStation("1", 43.5f, 4.0f, "75002", "Paris", "rue Cler", new Price(SP95, YESTERDAY, 1.10f));
		when(dataProvider.getGasStationsForPostCodes(any())).thenReturn(asList(gs));
		when(nameProvider.getById(any())).thenReturn(Optional.empty());

		Response resp = underTest.handle(buildTownInput(SP95, "Paris", "c05,75001,75002")).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertCard("Pompe à essence\nRue Cler, Paris\nSans Plomb 95 : 1€10", resp);
		assertSpeech("Une pompe vend du sans plomb 95 pour 1€10. Elle est située Rue Cler à Paris, et a actualisé ses "
				+ "tarifs hier.", resp);
		verify(dataProvider).getGasStationsForPostCodes("75001", "75002");
		verify(gasStationPriceSorter).sortGasStationsByIncreasingPricesForGasType(asList(gs), SP95);
		verify(nameProvider).getById("1");
	}

	@Test
	@Tags({ @Tag("happy"), @Tag("town") })
	void shouldSuggestE10AsAnAlternativeIfNoGasStationsSellAnySP95() {
		GasStation gs = new GasStation("1", 43.561f, 4.076f, "75002", "Paris", "rue Cler", new Price(E10, DATE, 1.10f));
		when(dataProvider.getGasStationsForPostCodes(any())).thenReturn(asList(gs));
		when(nameProvider.getById(any())).thenReturn(Optional.of("Pyves Gas"));

		Response resp = underTest.handle(buildTownInput(SP95, "Paris", "c05,75001,75002")).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertCard("Pyves Gas\nRue Cler, Paris\nE10 : 1€10", resp);
		assertSpeech("Je n'ai pas trouvé de pompe vendant du sans plomb 95. Cependant, Pyves Gas vend de l'E10 pour 1€10. "
				+ "Cette pompe est située Rue Cler à Paris, et a actualisé ses tarifs le 2018-04-04.", resp);
		verify(dataProvider).getGasStationsForPostCodes("75001", "75002");
		verify(gasStationPriceSorter).sortGasStationsByIncreasingPricesForGasType(asList(gs), SP95);
		verify(gasStationPriceSorter).sortGasStationsByIncreasingPricesForGasType(asList(gs), E10);
		verify(nameProvider).getById("1");
	}

	@ParameterizedTest
	@EnumSource(GasType.class)
	@Tags({ @Tag("not-found"), @Tag("town") })
	void shouldReturnNoGasStationFoundForTypeIfNoGasStationsSellTheRequestedGasTypeInRequestedTown(GasType gasType) {
		GasStation gs = new GasStation("1", 43.561f, 4.076f, "75001", "paris", "Place Vendôme");
		when(dataProvider.getGasStationsForPostCodes(any())).thenReturn(asList(gs));

		Response resp = underTest.handle(buildTownInput(gasType, "Paris", "c05,75001")).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertNull(resp.getCard());
		assertSpeech("Je n'ai pas trouvé de pompe vendant " + gasType.getSpeechText()
				+ " dans Paris. Éssayez un autre carburant ou une ville différente.", resp);
	}

	@ParameterizedTest
	@EnumSource(GasType.class)
	@Tags({ @Tag("not-found"), @Tag("radius") })
	void shouldReturnNoGasStationFoundForTypeIfNoGasStationsSellTheRequestedGasTypeInRequestedRadius(GasType gasType)
			throws Exception {
		Address address = new Address("54Bis rue Cler", "Paris", "75002");
		when(deviceAddressProvider.fetchAddress(any(), any(), any())).thenReturn(address);
		Position position = new Position(43.6f, 4.08f);
		when(positionProvider.getByAddress(any())).thenReturn(Optional.of(position));
		GasStation gs = new GasStation("1", 43.561f, 4.076f, "75001", "paris", "Place Vendôme");
		when(dataProvider.getGasStationsWithinRadius(any(), anyInt())).thenReturn(asList(gs));

		Response resp = underTest.handle(buildRadiusInput(gasType, "10", true)).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertNull(resp.getCard());
		assertSpeech("Je n'ai pas trouvé de pompe vendant " + gasType.getSpeechText() + " à moins de 10 kilomètres. "
				+ "Réessayez en spécifiant un autre carburant ou une distance plus grande.", resp);
	}

	@Test
	@Tags({ @Tag("not-found"), @Tag("town") })
	void shouldReturnNoGasStationFoundIfNoGasStationsInRequestedTown() {
		when(dataProvider.getGasStationsForPostCodes(any())).thenReturn(emptyList());

		Response resp = underTest.handle(buildTownInput(GAZOLE, "Paris", "c05,75003")).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertNull(resp.getCard());
		assertSpeech("Je n'ai pas trouvé de pompe dans Paris. Éssayez une ville différente.", resp);
	}

	@Test
	@Tags({ @Tag("not-found"), @Tag("radius") })
	void shouldReturnNoGasStationFoundIfNoGasStationsInRequestedRadius() throws Exception {
		Address address = new Address("54Bis rue Cler", "Paris", "75002");
		when(deviceAddressProvider.fetchAddress(any(), any(), any())).thenReturn(address);
		Position position = new Position(43.6f, 4.08f);
		when(positionProvider.getByAddress(any())).thenReturn(Optional.of(position));
		when(dataProvider.getGasStationsWithinRadius(any(), anyInt())).thenReturn(emptyList());

		Response resp = underTest.handle(buildRadiusInput(SP95, "10", true)).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertNull(resp.getCard());
		assertSpeech("Je n'ai pas trouvé de pompe à moins de 10 kilomètres. Réessayez en spécifiant une distance "
				+ "plus grande. Par exemple, dîtes simplement : \"le Sans Plomb 95 à moins de 15 kilomètres\".", resp);
	}

	@TestFactory
	@Tags({ @Tag("unsupported-gas"), @Tag("town"), @Tag("radius") })
	Stream<DynamicTest> shouldReturnUnsupportedGasTypeIfGasTypeCouldNotBeMatched() {
		HandlerInput input1 = buildTownInput("or noir", null, "Paris", "c05,75001,75002");
		HandlerInput input2 = buildRadiusInput("sans plomb 97", null, "2", true);

		return Stream.of(input1, input2).map(input -> dynamicTest(buildDynamicDisplayName(input),
				() -> {
					Response resp = underTest.handle(input).orElseThrow(MissingResponse::new);
					assertFalse(resp.getShouldEndSession());
					assertNull(resp.getCard());
					assertSpeech("Je n'ai pas compris le carburant demandé. Réessayez en spécifiant gazole, sans plomb 95, "
							+ "sans plomb 98, E10, E85 ou GPL. Par exemple, dîtes simplement : \"le gazole à Bordeaux\".",
							resp);
				}));
	}

	@ParameterizedTest
	@ValueSource(strings = { "0", "51", "" })
	@Tags({ @Tag("incorrect-radius"), @Tag("radius") })
	void shouldReturnIncorrectRadiusIfTheProvidedRadiusIsNotBetween1And20(String radius) {
		Response resp = underTest.handle(buildRadiusInput(SP95, radius, true)).orElseThrow(MissingResponse::new);

		assertFalse(resp.getShouldEndSession());
		assertNull(resp.getCard());
		assertSpeech("Veuillez fournir une distance comprise entre 1 et 50 kilomètres. Par exemple : "
				+ "\"le sans plomb 95 à moins de 10 kilomètres\".", resp);
	}

	@Test
	@Tags({ @Tag("address-inaccessible"), @Tag("radius") })
	void shouldReturnErrorIfDeviceAddressProviderThrowsAddressInaccessibleException() throws Exception {
		when(deviceAddressProvider.fetchAddress(any(), any(), any())).thenThrow(AddressInaccessibleException.class);

		Response resp = underTest.handle(buildRadiusInput(SP95, "10", true)).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertNull(resp.getCard());
		assertSpeech("Alexa a retourné une erreur. Réessayez plus tard, ou bien précisez un nom de ville ou de département.",
				resp);
	}

	@Test
	@Tags({ @Tag("no-perms"), @Tag("radius") })
	void shouldRequestPermissionsIfNotPresent() {
		Response resp = underTest.handle(buildRadiusInput(SP95, "10", false)).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertCardWithPermissions(resp);
		assertSpeech("J'ai besoin de votre adresse pour trouver les pompes à proximité. Veuillez autoriser l'accès "
				+ "dans l'application Alexa, ou bien précisez un nom de ville ou de département.", resp);
	}

	@Test
	@Tags({ @Tag("address-forbidden"), @Tag("radius") })
	void shouldRequestPermissionsIfDeviceAddressProviderThrowsAnAddressForbiddenException() throws Exception {
		when(deviceAddressProvider.fetchAddress(any(), any(), any())).thenThrow(AddressForbiddenException.class);

		Response resp = underTest.handle(buildRadiusInput(SP95, "10", true)).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertCardWithPermissions(resp);
		assertSpeech("J'ai besoin de votre adresse pour trouver les pompes à proximité. Veuillez autoriser l'accès "
				+ "dans l'application Alexa, ou bien précisez un nom de ville ou de département.", resp);
	}

	@Test
	@Tags({ @Tag("unsupported-location"), @Tag("town") })
	void shouldReturnUnsupportedLocationIfLocationIsMissing() {
		Response resp = underTest.handle(buildTownInput(GAZOLE, "New-York", null)).orElseThrow(MissingResponse::new);

		assertFalse(resp.getShouldEndSession());
		assertNull(resp.getCard());
		assertSpeech("Je n'ai pas trouvé d'informations pour cette localisation géographique. Réessayez en énonçant "
				+ "clairement le nom de ville ou de département, ou bien spécifiez un lieu différent.", resp);
	}

	@Test
	@Tags({ @Tag("missing-slot") })
	void shouldDelegateDirectiveForMissingGasType() throws Exception {
		Response resp = underTest.handle(buildIntentInputWithNoGasValue("AnyIntent")).orElseThrow(MissingResponse::new);

		assertNotNull(resp.getDirectives());
	}

	@Test
	@Tags({ @Tag("unknown-position"), @Tag("radius") })
	void shouldReturnUnknownPositionIfThePositionCouldNotBeDetermined() throws Exception {
		Address address = new Address("54 rue Cler", "Paris", "75002");
		when(deviceAddressProvider.fetchAddress(any(), any(), any())).thenReturn(address);
		when(positionProvider.getByAddress(any())).thenReturn(Optional.empty());

		Response resp = underTest.handle(buildRadiusInput(SP95, "10", true)).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertNull(resp.getCard());
		assertSpeech("Je n'ai pas réussi à déterminer votre position géographique avec l'adresse renseignée dans "
				+ "votre Amazon Echo. Réessayez plus tard, ou bien précisez un nom de ville ou de département.", resp);
	}

}
