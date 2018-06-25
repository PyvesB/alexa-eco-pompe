package io.github.pyvesb.alexaecopompe.handlers;

import static com.amazon.ask.request.Predicates.requestType;
import static io.github.pyvesb.alexaecopompe.speech.Messages.ADDRESS_ERROR;
import static io.github.pyvesb.alexaecopompe.speech.Messages.INCORRECT_RADIUS;
import static io.github.pyvesb.alexaecopompe.speech.Messages.LOCATION_BAD_REQUEST;
import static io.github.pyvesb.alexaecopompe.speech.Messages.MISSING_PERMS;
import static io.github.pyvesb.alexaecopompe.speech.Messages.NAME;
import static io.github.pyvesb.alexaecopompe.speech.Messages.NO_STATION_FOUND;
import static io.github.pyvesb.alexaecopompe.speech.Messages.POSITION_UNKNOWN;
import static io.github.pyvesb.alexaecopompe.speech.Messages.RADIUS_BAD_REQUEST;
import static io.github.pyvesb.alexaecopompe.speech.Messages.STATION_FOUND;
import static io.github.pyvesb.alexaecopompe.speech.Messages.STATION_FOUND_E10;
import static io.github.pyvesb.alexaecopompe.speech.Messages.UNSUPPORTED;
import static io.github.pyvesb.alexaecopompe.speech.Messages.UNSUPPORTED_GAS_TYPE;
import static io.github.pyvesb.alexaecopompe.speech.Messages.UNSUPPORTED_LOCATION;
import static java.lang.System.getenv;
import static java.util.Collections.singletonList;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.User;
import com.amazon.ask.model.interfaces.system.SystemState;
import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.Value;
import com.amazon.ask.model.slu.entityresolution.ValueWrapper;
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

public class MainIntentHandler implements RequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(MainIntentHandler.class);
	private static final List<String> ADDRESS_PERMS = singletonList("read::alexa:device:all:address");
	private static final Slot DEFAULT_RADIUS = Slot.builder().withName("radius").withValue("5").build();
	private static final int RADIUS_UPPER_BOUND = 20;

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
	public boolean canHandle(HandlerInput input) {
		return input.matches(requestType(IntentRequest.class));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		RequestEnvelope envelope = input.getRequestEnvelope();
		Intent intent = ((IntentRequest) envelope.getRequest()).getIntent();
		String intentName = intent.getName();
		Session session = envelope.getSession();
		LOGGER.info("Received intent (session={}, name={})", session.getSessionId(), intentName);

		Map<String, Slot> slots = intent.getSlots();
		if ("GasTown".equals(intentName)) {
			return handleLocationRequest(input.getResponseBuilder(), slots.get("gas"), slots.get("town"));
		} else if ("GasDepartment".equals(intentName)) {
			return handleLocationRequest(input.getResponseBuilder(), slots.get("gas"), slots.get("department"));
		} else if ("GasRadius".equals(intentName)) {
			return handleRadiusRequest(input.getResponseBuilder(), slots.get("gas"), slots.get("radius"),
					envelope.getContext(), session.getUser());
		} else if ("GasNearby".equals(intentName)) {
			return handleRadiusRequest(input.getResponseBuilder(), slots.get("gas"), DEFAULT_RADIUS, envelope.getContext(),
					session.getUser());
		}
		return input.getResponseBuilder().withSpeech(UNSUPPORTED).withReprompt(UNSUPPORTED).build();
	}

	private Optional<Response> handleLocationRequest(ResponseBuilder respBuilder, Slot gasSlot, Slot locationSlot) {
		if (gasSlot != null && locationSlot != null) {
			Optional<String> gasId = getSlotId(gasSlot);
			Optional<String> locationId = getSlotId(locationSlot);
			if (!gasId.isPresent()) {
				LOGGER.warn("Unsupported gas type (gas={})", gasSlot.getValue());
				return respBuilder.withSpeech(UNSUPPORTED_GAS_TYPE).withReprompt(UNSUPPORTED_GAS_TYPE).build();
			} else if (!locationId.isPresent()) {
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
			return handleGasStationList(respBuilder, gasType, gasStations, Optional.ofNullable(town));
		}
		LOGGER.warn("Null slot(s) (gasSlot={}, locationSlot={})", gasSlot, locationSlot);
		return respBuilder.withSpeech(LOCATION_BAD_REQUEST).withReprompt(LOCATION_BAD_REQUEST).build();
	}

	private Optional<Response> handleRadiusRequest(ResponseBuilder respBuilder, Slot gasSlot, Slot radiusSlot,
			Context context, User user) {
		if (gasSlot != null && radiusSlot != null) {
			Optional<String> gasId = getSlotId(gasSlot);
			int radius = NumberUtils.toInt(radiusSlot.getValue());
			if (!gasId.isPresent()) {
				LOGGER.warn("Unsupported gas type (gas={})", gasSlot.getValue());
				return respBuilder.withSpeech(UNSUPPORTED_GAS_TYPE).withReprompt(UNSUPPORTED_GAS_TYPE).build();
			} else if (radius <= 0 || radius > RADIUS_UPPER_BOUND) {
				LOGGER.info("Incorrect radius (radius={})", radiusSlot.getValue());
				return respBuilder.withSpeech(INCORRECT_RADIUS).withReprompt(INCORRECT_RADIUS).build();
			} else if (user.getPermissions() == null) {
				LOGGER.info("No permissions - null");
				return respBuilder.withSpeech(MISSING_PERMS).withAskForPermissionsConsentCard(ADDRESS_PERMS)
						.withShouldEndSession(true).build();
			}
			SystemState systemState = context.getSystem();
			try {
				Address address = deviceAddressProvider.fetchAddress(systemState.getApiEndpoint(),
						systemState.getDevice().getDeviceId(), systemState.getApiAccessToken());
				Optional<Position> position = positionProvider.getByAddress(address);
				if (position.isPresent()) {
					List<GasStation> gasStations = dataProvider.getGasStationsWithinRadius(position.get(), radius);
					GasType gasType = GasType.fromId(gasId.get());
					LOGGER.info("Radius request (gas={}, radius={})", gasType, radius);
					return handleGasStationList(respBuilder, gasType, gasStations, Optional.empty());
				}
				LOGGER.warn("Unknown position (address={})", address);
				return respBuilder.withSpeech(POSITION_UNKNOWN).withShouldEndSession(true).build();
			} catch (AddressForbiddenException e) {
				LOGGER.info("No permissions - exception");
				return respBuilder.withSpeech(MISSING_PERMS).withAskForPermissionsConsentCard(ADDRESS_PERMS)
						.withShouldEndSession(true).build();
			} catch (AddressInaccessibleException e) {
				LOGGER.error("Amazon address error (endpoint={})", systemState.getApiEndpoint(), e);
				return respBuilder.withSpeech(ADDRESS_ERROR).withShouldEndSession(true).build();
			}
		}
		LOGGER.warn("Null slot(s) (gasSlot={}, radiusSlot={})", gasSlot, radiusSlot);
		return respBuilder.withSpeech(RADIUS_BAD_REQUEST).withReprompt(RADIUS_BAD_REQUEST).build();
	}

	private Optional<String> getSlotId(Slot slot) {
		// The slot id is treated at the same level as the slot value in the Alexa console but unfortunately there
		// doesn't seem to be any cleaner way than chaining all these method calls and checks to retrieve it.
		Resolutions resolutions = slot.getResolutions();
		if (resolutions != null && !resolutions.getResolutionsPerAuthority().isEmpty()) {
			List<ValueWrapper> values = resolutions.getResolutionsPerAuthority().get(0).getValues();
			if (values != null && !values.isEmpty()) {
				Value value = values.get(0).getValue();
				if (value != null) {
					return Optional.ofNullable(value.getId());
				}
			}
		}
		return Optional.empty();
	}

	private Optional<Response> handleGasStationList(ResponseBuilder respBuilder, GasType gasType,
			List<GasStation> gasStations, Optional<String> town) {
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
		}
		String text = StringUtils.replaceEach(NO_STATION_FOUND, new String[] { "$TYPE", "$LOCATION" },
				new String[] { gasType.getSpeechText(), town.orElse("les environs") });
		LOGGER.info("No station found (gas={}, location={})", gasType, town.orElse("other"));
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
