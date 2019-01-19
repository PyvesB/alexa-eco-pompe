package utils;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import java.util.HashMap;
import java.util.Map;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.Device;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.PermissionStatus;
import com.amazon.ask.model.Permissions;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Scope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.SessionEndedRequest;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.SupportedInterfaces;
import com.amazon.ask.model.User;
import com.amazon.ask.model.interfaces.geolocation.GeolocationInterface;
import com.amazon.ask.model.interfaces.geolocation.GeolocationState;
import com.amazon.ask.model.interfaces.system.SystemState;
import com.amazon.ask.model.slu.entityresolution.Resolution;
import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.Status;
import com.amazon.ask.model.slu.entityresolution.StatusCode;
import com.amazon.ask.model.slu.entityresolution.Value;
import com.amazon.ask.model.slu.entityresolution.ValueWrapper;

import io.github.pyvesb.alexaecopompe.domain.GasType;

public class InputBuilder {

	public static final String API_ACCESS_TOKEN = "some-token";
	public static final String API_ENDPOINT = "some-endpoint";
	public static final String DEVICE_ID = "some-device";

	private static final String SESSION_ID = "amzn1.echo-api.session.f7c13f4e-7b37-422d-89ba-45bfb1b00eed";

	public static HandlerInput buildLaunchInput() {
		return buildInput(LaunchRequest.builder().build());
	}

	public static HandlerInput buildEndedInput() {
		return buildInput(SessionEndedRequest.builder().build());
	}

	public static HandlerInput buildIntentInput(String intentName) {
		Intent intent = Intent.builder().withName(intentName).build();
		IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
		return buildInput(intentRequest);
	}

	public static HandlerInput buildIntentInputWithNoGasValue(String intentName) {
		Slot gasSlot = Slot.builder().withName("gas").build();
		Intent intent = Intent.builder().withName(intentName).withSlots(singletonMap("gas", gasSlot)).build();
		IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
		return buildInput(intentRequest);
	}

	public static HandlerInput buildDepartmentInput(GasType gasType, String department, String deparmentId) {
		Map<String, Slot> slots = buildSlots(gasType.name(), gasType.getIdString(), "department", department, deparmentId);
		return buildIntentInput("GasDepartment", slots);
	}

	public static HandlerInput buildTownInput(GasType gasType, String town, String townId) {
		return buildTownInput(gasType.name(), gasType.getIdString(), town, townId);
	}

	public static HandlerInput buildTownInput(String gas, String gasId, String town, String townId) {
		Map<String, Slot> slots = buildSlots(gas, gasId, "town", town, townId);
		return buildIntentInput("GasTown", slots);
	}

	public static HandlerInput buildRadiusInput(GasType gasType, String radius) {
		return buildRadiusInput(gasType.name(), gasType.getIdString(), radius);
	}

	public static HandlerInput buildRadiusInput(String gas, String gasId, String radius) {
		Map<String, Slot> slots = buildSlots(gas, gasId, "radius", radius, null);
		return buildIntentInput("GasRadius", slots);
	}

	public static HandlerInput buildRadiusGeoInput(GasType gasType, String radius, GeolocationState geolocation,
			PermissionStatus permissionStatus) {
		Map<String, Slot> slots = buildSlots(gasType.name(), gasType.getIdString(), "radius", radius, null);
		Intent intent = Intent.builder().withName("GasRadius").withSlots(slots).build();
		IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
		Scope scope = Scope.builder().withStatus(permissionStatus).build();
		Permissions permissions = Permissions.builder()
				.withScopes(singletonMap("alexa::devices:all:geolocation:read", scope)).build();
		User user = User.builder().withPermissions(permissions).build();
		GeolocationInterface geoInterface = GeolocationInterface.builder().build();
		SupportedInterfaces supportedInterfaces = SupportedInterfaces.builder().withGeolocation(geoInterface).build();
		return buildInput(intentRequest, supportedInterfaces, geolocation, user);
	}

	public static HandlerInput buildNearbyInput(GasType gasType) {
		Resolutions resolutions = buildResolutions(gasType.name(), gasType.getIdString());
		Slot gasSlot = Slot.builder().withName("gas").withResolutions(resolutions).withValue(gasType.name()).build();
		return buildIntentInput("GasNearby", singletonMap("gas", gasSlot));
	}

	private static Map<String, Slot> buildSlots(String gas, String gasId, String otherName, String otherValue,
			String otherId) {
		Map<String, Slot> slots = new HashMap<>();
		Resolutions gasResolutions = buildResolutions(gas, gasId);
		Slot gasSlot = Slot.builder().withName("gas").withResolutions(gasResolutions).withValue(gas).build();
		slots.put("gas", gasSlot);
		Resolutions otherResolutions = buildResolutions(otherName, otherId);
		Slot otherSlot = Slot.builder().withName(otherName).withValue(otherValue).withResolutions(otherResolutions).build();
		slots.put(otherName, otherSlot);
		return slots;
	}

	private static Resolutions buildResolutions(String name, String id) {
		Resolutions resolutions = null;
		Value value = Value.builder().withName(name).withId(id).build();
		ValueWrapper valueWrapper = ValueWrapper.builder().withValue(value).build();
		StatusCode statusCode = id == null ? StatusCode.ER_SUCCESS_NO_MATCH : StatusCode.ER_SUCCESS_MATCH;
		Status status = Status.builder().withCode(statusCode).build();
		Resolution resolution = Resolution.builder().withValues(singletonList(valueWrapper)).withStatus(status).build();
		resolutions = Resolutions.builder().withResolutionsPerAuthority(singletonList(resolution)).build();
		return resolutions;
	}

	private static HandlerInput buildIntentInput(String intentName, Map<String, Slot> slots) {
		Intent intent = Intent.builder().withName(intentName).withSlots(slots).build();
		IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
		return buildInput(intentRequest);
	}

	private static HandlerInput buildInput(Request speechletRequest) {
		return buildInput(speechletRequest, SupportedInterfaces.builder().build(), null, null);
	}

	private static HandlerInput buildInput(Request request, SupportedInterfaces supportedInterfaces,
			GeolocationState geolocation, User user) {
		Device device = Device.builder().withDeviceId(DEVICE_ID).withSupportedInterfaces(supportedInterfaces).build();
		SystemState systemState = SystemState.builder().withApiAccessToken(API_ACCESS_TOKEN).withApiEndpoint(API_ENDPOINT)
				.withDevice(device).withUser(user).build();
		Context context = Context.builder().withSystem(systemState).withGeolocation(geolocation).build();
		Session session = Session.builder().withSessionId(SESSION_ID).build();
		RequestEnvelope envelope = RequestEnvelope.builder().withContext(context).withRequest(request).withSession(session)
				.build();
		return HandlerInput.builder().withRequestEnvelope(envelope).build();
	}

	private InputBuilder() {
		// Not used.
	}

}
