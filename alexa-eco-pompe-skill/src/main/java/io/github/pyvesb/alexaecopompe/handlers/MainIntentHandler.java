package io.github.pyvesb.alexaecopompe.handlers;

import static io.github.pyvesb.alexaecopompe.speech.Messages.ADDRESS_ERROR;
import static io.github.pyvesb.alexaecopompe.speech.Messages.INCORRECT_RADIUS;
import static io.github.pyvesb.alexaecopompe.speech.Messages.MISSING_ADDRESS_PERMS;
import static io.github.pyvesb.alexaecopompe.speech.Messages.MISSING_ADDRESS_PERMS_NO_GEO;
import static io.github.pyvesb.alexaecopompe.speech.Messages.MISSING_GEO_PERMS;
import static io.github.pyvesb.alexaecopompe.speech.Messages.NAME;
import static io.github.pyvesb.alexaecopompe.speech.Messages.NO_STATION_FOR_TYPE_RADIUS;
import static io.github.pyvesb.alexaecopompe.speech.Messages.NO_STATION_FOR_TYPE_TOWN;
import static io.github.pyvesb.alexaecopompe.speech.Messages.NO_STATION_RADIUS;
import static io.github.pyvesb.alexaecopompe.speech.Messages.NO_STATION_TOWN;
import static io.github.pyvesb.alexaecopompe.speech.Messages.POSITION_UNKNOWN;
import static io.github.pyvesb.alexaecopompe.speech.Messages.STATION_FOUND;
import static io.github.pyvesb.alexaecopompe.speech.Messages.STATION_FOUND_E10;
import static io.github.pyvesb.alexaecopompe.speech.Messages.UNSUPPORTED_GAS_TYPE;
import static io.github.pyvesb.alexaecopompe.speech.Messages.UNSUPPORTED_LOCATION;
import static java.lang.System.getenv;
import static java.util.Collections.singletonList;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.Device;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.PermissionStatus;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.User;
import com.amazon.ask.model.interfaces.geolocation.Coordinate;
import com.amazon.ask.model.interfaces.geolocation.GeolocationState;
import com.amazon.ask.model.interfaces.system.SystemState;
import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.StatusCode;
import com.amazon.ask.response.ResponseBuilder;

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
import io.github.pyvesb.alexaecopompe.speech.Normalisers;
import io.github.pyvesb.alexaecopompe.utils.GasStationPriceSorter;
import io.github.pyvesb.alexaecopompe.utils.PostCodesExtractor;

public class MainIntentHandler implements IntentRequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(MainIntentHandler.class);

	private static final String ADDRESS_PERM = "read::alexa:device:all:address";
	private static final String GEO_PERM = "alexa::devices:all:geolocation:read";

	private static final Slot DEFAULT_RADIUS = Slot.builder().withName("radius").withValue("5").build();
	private static final int RADIUS_UPPER_BOUND = 50;

	private static final double COORDINATE_ACCURACY_METERS = 1000;
	private static final int GEOLOCATION_STALENESS_SECONDS = 300;

	private final DataProvider dataProvider;
	private final NameProvider nameProvider;
	private final PositionProvider positionProvider;
	private final DeviceAddressProvider deviceAddressProvider;
	private final GasStationPriceSorter gasStationPriceSorter;
	private final DecimalFormat euroFormat;

	public MainIntentHandler() {
		this(new DataProvider(getenv("DATA_URL"), Long.parseLong(getenv("DATA_STALENESS_MILLIS"))),
				new NameProvider(),
				new PositionProvider(getenv("POSITION_ENPOINT"), getenv("USER_AGENT"),
						Integer.parseInt(getenv("TIMEOUT_MILLIS")), getenv("LAT_PATH"), getenv("LON_PATH")),
				new DeviceAddressProvider(Integer.parseInt(getenv("TIMEOUT_MILLIS"))),
				new GasStationPriceSorter(Long.parseLong(getenv("PRICE_STALENESS_DAYS"))));
	}

	MainIntentHandler(DataProvider dataProvider, NameProvider nameProvider, PositionProvider positionProvider,
			DeviceAddressProvider deviceAddressProvider, GasStationPriceSorter gasStationPriceSorter) {
		this.dataProvider = dataProvider;
		this.positionProvider = positionProvider;
		this.nameProvider = nameProvider;
		this.deviceAddressProvider = deviceAddressProvider;
		this.gasStationPriceSorter = gasStationPriceSorter;
		DecimalFormatSymbols euroSymbol = new DecimalFormatSymbols();
		euroSymbol.setDecimalSeparator('€');
		// Formats prices to values such as "1€22" or "1€589", which will be correctly spoken out loud by Alexa.
		euroFormat = new DecimalFormat("0.00#", euroSymbol);
	}

	@Override
	public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
		return true;
	}

	@Override
	public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
		RequestEnvelope envelope = input.getRequestEnvelope();
		Intent intent = intentRequest.getIntent();
		String intentName = intent.getName();
		Session session = envelope.getSession();
		LOGGER.info("Received intent (session={}, name={})", session.getSessionId(), intentName);

		Map<String, Slot> slots = intent.getSlots();
		Slot gasSlot = slots.get("gas");
		if (gasSlot.getValue() == null) {
			return handleMissingGasValue(input, intent);
		} else if ("GasTown".equals(intentName)) {
			return handleLocationRequest(input.getResponseBuilder(), gasSlot, slots.get("town"));
		} else if ("GasDepartment".equals(intentName)) {
			return handleLocationRequest(input.getResponseBuilder(), gasSlot, slots.get("department"));
		} else if ("GasRadius".equals(intentName)) {
			return handleRadiusRequest(input.getResponseBuilder(), gasSlot, slots.get("radius"), envelope.getContext(),
					session.getUser());
		}
		return handleRadiusRequest(input.getResponseBuilder(), gasSlot, DEFAULT_RADIUS, envelope.getContext(),
				session.getUser());
	}

	private Optional<Response> handleMissingGasValue(HandlerInput input, Intent intent) {
		LOGGER.info("Gas type not specified in intent");
		return input.getResponseBuilder().addDelegateDirective(intent).build();
	}

	private Optional<Response> handleLocationRequest(ResponseBuilder respBuilder, Slot gasSlot, Slot locationSlot) {
		Optional<String> gasId = getSlotId(gasSlot);
		if (!gasId.isPresent()) {
			LOGGER.warn("Unsupported gas type (gas={})", gasSlot.getValue());
			return respBuilder.withSpeech(UNSUPPORTED_GAS_TYPE).withReprompt(UNSUPPORTED_GAS_TYPE).build();
		}
		Optional<String> locationId = getSlotId(locationSlot);
		if (!locationId.isPresent()) {
			LOGGER.warn("Unsupported location (location={})", locationSlot.getValue());
			return respBuilder.withSpeech(UNSUPPORTED_LOCATION).withReprompt(UNSUPPORTED_LOCATION).build();
		}
		List<GasStation> gasStations;
		String town = null;
		if ("town".equals(locationSlot.getName())) {
			gasStations = dataProvider.getGasStationsForPostCodes(PostCodesExtractor.from(locationId.get()));
			town = locationSlot.getValue();
		} else {
			gasStations = dataProvider.getGasStationsForDepartment(locationId.get());
		}
		GasType gasType = GasType.fromId(gasId.get());
		LOGGER.info("Location request (gas={}, location={})", gasType, locationSlot.getValue());
		return handleGasStationList(respBuilder, gasType, gasStations, Optional.ofNullable(town), Optional.empty());
	}

	private Optional<Response> handleRadiusRequest(ResponseBuilder respBuilder, Slot gasSlot, Slot radiusSlot,
			Context context, User user) {
		Optional<String> gasId = getSlotId(gasSlot);
		if (!gasId.isPresent()) {
			LOGGER.warn("Unsupported gas type (gas={})", gasSlot.getValue());
			return respBuilder.withSpeech(UNSUPPORTED_GAS_TYPE).withReprompt(UNSUPPORTED_GAS_TYPE).build();
		}
		int radius = NumberUtils.toInt(radiusSlot.getValue());
		if (radius <= 0 || radius > RADIUS_UPPER_BOUND) {
			LOGGER.info("Incorrect radius (radius={})", radiusSlot.getValue());
			return respBuilder.withSpeech(INCORRECT_RADIUS).withReprompt(INCORRECT_RADIUS).build();
		}

		SystemState system = context.getSystem();
		Device device = system.getDevice();
		Optional<Position> position;
		if (isGeolocationAvailable(context)) {
			LOGGER.info("Using device geolocation (device={})", device.getDeviceId());
			position = positionProvider.getByGeolocation(context.getGeolocation());
		} else if (isGeolocationCompatible(device) && isMissingGeolocationPermission(user)) {
			return handleMissingPermissions(respBuilder, GEO_PERM, MISSING_GEO_PERMS);
		} else {
			LOGGER.info("Using device address (device={})", device.getDeviceId());
			try {
				Address address = deviceAddressProvider.fetchAddress(system.getApiEndpoint(),
						device.getDeviceId(), system.getApiAccessToken());
				position = positionProvider.getByAddress(address);
			} catch (AddressForbiddenException e) {
				String speech = isGeolocationCompatible(device) ? MISSING_ADDRESS_PERMS_NO_GEO : MISSING_ADDRESS_PERMS;
				return handleMissingPermissions(respBuilder, ADDRESS_PERM, speech);
			} catch (AddressInaccessibleException e) {
				LOGGER.error("Amazon address error (endpoint={})", system.getApiEndpoint(), e);
				return respBuilder.withSpeech(ADDRESS_ERROR).withShouldEndSession(true).build();
			}
		}

		if (position.isPresent()) {
			List<GasStation> gasStations = dataProvider.getGasStationsWithinRadius(position.get(), radius);
			GasType gasType = GasType.fromId(gasId.get());
			LOGGER.info("Radius request (gas={}, radius={})", gasType, radius);
			return handleGasStationList(respBuilder, gasType, gasStations, Optional.empty(), Optional.of(radius));
		}
		return respBuilder.withSpeech(POSITION_UNKNOWN).withShouldEndSession(true).build();
	}

	private boolean isMissingGeolocationPermission(User user) {
		return user.getPermissions().getScopes().get(GEO_PERM).getStatus() != PermissionStatus.GRANTED;
	}

	private boolean isGeolocationCompatible(Device device) {
		return device.getSupportedInterfaces().getGeolocation() != null;
	}

	private boolean isGeolocationAvailable(Context context) {
		GeolocationState geolocation = context.getGeolocation();
		if (geolocation != null) {
			Coordinate coordinate = geolocation.getCoordinate();
			if (coordinate != null) {
				long freshness = OffsetDateTime.now().toEpochSecond()
						- OffsetDateTime.parse(geolocation.getTimestamp()).toEpochSecond();
				LOGGER.info("Geolocation (accuracy={}, freshness={}s)", coordinate.getAccuracyInMeters(), freshness);
				return coordinate.getAccuracyInMeters() < COORDINATE_ACCURACY_METERS
						&& freshness <= GEOLOCATION_STALENESS_SECONDS;
			}
		}
		return false;
	}

	private Optional<Response> handleMissingPermissions(ResponseBuilder respBuilder, String perm, String speech) {
		LOGGER.info("Missing permissions (perms={})", perm);
		return respBuilder.withAskForPermissionsConsentCard(singletonList(perm)).withSpeech(speech)
				.withShouldEndSession(true).build();
	}

	private Optional<String> getSlotId(Slot slot) {
		// The slot id is treated at the same level as the slot value in the Alexa console but unfortunately there
		// doesn't seem to be any cleaner way than chaining all these method calls and checks to retrieve it.
		Resolutions resolutions = slot.getResolutions();
		if (resolutions != null) {
			return resolutions.getResolutionsPerAuthority().stream()
					.filter(r -> r.getStatus().getCode() == StatusCode.ER_SUCCESS_MATCH)
					.map(r -> r.getValues().get(0).getValue().getId())
					.findAny();
		}
		return Optional.empty();
	}

	private Optional<Response> handleGasStationList(ResponseBuilder respBuilder, GasType gasType,
			List<GasStation> gasStations, Optional<String> town, Optional<Integer> radius) {
		if (!gasStations.isEmpty()) {
			gasStationPriceSorter.sortGasStationsByIncreasingPricesForGasType(gasStations, gasType);
			GasStation cheapestGasStation = gasStations.get(0);
			Optional<Price> price = cheapestGasStation.getPriceForGasType(gasType);
			if (price.isPresent()) {
				LOGGER.info("Station found (gas={}, id={})", gasType, cheapestGasStation.getId());
				return buildGasStationResponse(respBuilder, cheapestGasStation, price.get(), town, STATION_FOUND);
			} else if (gasType == GasType.SP95) {
				// SP95 and E10 are interchangeable on most vehicles, see if E10 is available if no SP95 was found.
				gasStationPriceSorter.sortGasStationsByIncreasingPricesForGasType(gasStations, GasType.E10);
				cheapestGasStation = gasStations.get(0);
				price = cheapestGasStation.getPriceForGasType(GasType.E10);
				if (price.isPresent()) {
					LOGGER.info("Other E10 station (gas=E10, id={})", cheapestGasStation.getId());
					return buildGasStationResponse(respBuilder, cheapestGasStation, price.get(), town, STATION_FOUND_E10);
				}
			}
			String text = radius.isPresent()
					? StringUtils.replaceEach(NO_STATION_FOR_TYPE_RADIUS, new String[] { "$TYPE", "$RADIUS" },
							new String[] { gasType.getSpeechText(), radius.get().toString() })
					: StringUtils.replaceEach(NO_STATION_FOR_TYPE_TOWN, new String[] { "$TYPE", "$LOCATION" },
							new String[] { gasType.getSpeechText(), town.orElse("ce département") });
			LOGGER.info("No station found for type (gas={}, location={})", gasType,
					town.orElseGet(() -> radius.get() + "km"));
			return respBuilder.withSpeech(text).withShouldEndSession(true).build();
		}
		String text = radius.isPresent()
				? StringUtils.replaceEach(NO_STATION_RADIUS, new String[] { "$RADIUS", "$TYPE", "$BIGGER_RADIUS" },
						new String[] { radius.get().toString(), gasType.getDisplayName(),
								Integer.toString(Math.min(RADIUS_UPPER_BOUND, radius.get() + 5)) })
				: StringUtils.replaceOnce(NO_STATION_TOWN, "$LOCATION", town.orElse("ce département"));
		LOGGER.info("No station found (location={})", town.orElseGet(() -> radius.get() + "km"));
		return respBuilder.withSpeech(text).withShouldEndSession(true).build();
	}

	private Optional<Response> buildGasStationResponse(ResponseBuilder respBuilder, GasStation gasStation, Price price,
			Optional<String> town, String baseText) {
		Optional<String> gsName = nameProvider.getById(gasStation.getId());
		String euroText = euroFormat.format(price.getValue());
		String address = Normalisers.normaliseAddress(gasStation.getAddress());
		String townText = Normalisers.normaliseTown(town.orElse(gasStation.getTown()));

		String cardText = gsName.orElse("Pompe à essence") + "\n" + address + ", " + townText + "\n"
				+ price.getType().getDisplayName() + " : " + euroText;

		String updatedDateText = getUpdatedDateText(price);
		String speechText = StringUtils.replaceEach(baseText,
				new String[] { "$NAME", "$TYPE", "$PRICE", "$SUBJECT", "$ADDRESS", "$TOWN", "$DATE" },
				new String[] { gsName.orElse("Une pompe"), price.getType().getSpeechText(), euroText,
						gsName.isPresent() ? "Cette pompe" : "Elle", address, townText, updatedDateText });

		return respBuilder.withSpeech(speechText).withSimpleCard(NAME, cardText).withShouldEndSession(true).build();
	}

	private String getUpdatedDateText(Price price) {
		if (price.getUpdated().equals(LocalDate.now())) {
			return "aujourd'hui";
		}
		if (price.getUpdated().equals(LocalDate.now().minusDays(1))) {
			return "hier";
		}
		// Return values similar to "le 2018-05-03", which will be correctly spoken out loud by Alexa.
		return "le " + price.getUpdated();
	}

}
