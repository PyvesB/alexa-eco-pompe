package utils;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.Map;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.Device;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Permissions;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.SessionEndedRequest;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.User;
import com.amazon.ask.model.interfaces.system.SystemState;
import com.amazon.ask.model.slu.entityresolution.Resolution;
import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.Value;
import com.amazon.ask.model.slu.entityresolution.ValueWrapper;

import io.github.pyvesb.alexaecopompe.domain.GasType;

public class InputBuilder {

	public static final String API_ACCESS_TOKEN = "some-token";
	public static final String API_ENDPOINT = "some-endpoint";
	public static final String DEVICE_ID = "some-device";

	private static final String SESSION_ID = "amzn1.echo-api.session.f7c13f4e-7b37-422d-89ba-45bfb1b00eed";

	public static HandlerInput buildLaunchInput() {
		LaunchRequest launchRequest = LaunchRequest.builder().build();
		return buildInput(launchRequest);
	}

	public static HandlerInput buildEndedInput() {
		return buildInput(SessionEndedRequest.builder().build());
	}

	public static HandlerInput buildIntentInput(String intentName) {
		Intent intent = Intent.builder().withName(intentName).build();
		IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
		return buildInput(intentRequest);
	}

	public static HandlerInput buildIntentInput(String intentName, Map<String, Slot> slots) {
		Intent intent = Intent.builder().withName(intentName).withSlots(slots).build();
		IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
		return buildInput(intentRequest);
	}

	public static HandlerInput buildDepartmentInput(GasType gasType, String department, String deparmentId) {
		Map<String, Slot> slots = new HashMap<>();
		if (gasType != null) {
			Resolutions resolutions = buildResolutions(gasType.name(), Integer.toString(gasType.getId()));
			slots.put("gas", Slot.builder().withName("gas").withResolutions(resolutions).withValue(gasType.name()).build());
		}
		if (department != null) {
			slots.put("department", Slot.builder().withName("department")
					.withResolutions(buildResolutions("department", deparmentId)).withValue(department).build());
		}
		return buildIntentInput("GasDepartment", slots);
	}

	public static HandlerInput buildTownInput(GasType gasType, String town, String townId) {
		return buildTownInput(gasType == null ? null : gasType.name(),
				gasType == null ? null : Integer.toString(gasType.getId()), town, townId);
	}

	public static HandlerInput buildTownInput(String gas, String gasId, String town, String townId) {
		Map<String, Slot> slots = new HashMap<>();
		if (gas != null) {
			slots.put("gas", Slot.builder().withName("gas").withResolutions(buildResolutions("gas", gasId)).withValue(gas)
					.build());
		}
		if (town != null) {
			slots.put("town", Slot.builder().withName("town").withResolutions(buildResolutions("town", townId))
					.withValue(town).build());
		}
		return buildIntentInput("GasTown", slots);
	}

	public static HandlerInput buildRadiusInput(GasType gasType, String radius, boolean hasPerms) {
		return buildRadiusInput(gasType == null ? null : gasType.name(),
				gasType == null ? null : Integer.toString(gasType.getId()), radius, hasPerms);
	}

	public static HandlerInput buildRadiusInput(String gas, String gasId, String radius, boolean hasPerms) {
		Map<String, Slot> slots = new HashMap<>();
		if (gas != null) {
			slots.put("gas", Slot.builder().withName("gas").withResolutions(buildResolutions(gas, gasId)).withValue(gas)
					.build());
		}
		if (radius != null) {
			slots.put("radius", Slot.builder().withName("radius").withValue(radius).build());
		}
		return hasPerms ? buildIntentInputWithPermissions("GasRadius", slots) : buildIntentInput("GasRadius", slots);
	}

	public static HandlerInput buildNearbyInput(GasType gasType, boolean hasPerms) {
		Map<String, Slot> slots = new HashMap<>();
		if (gasType != null) {
			Resolutions resolutions = buildResolutions(gasType.name(), Integer.toString(gasType.getId()));
			slots.put("gas", Slot.builder().withName("gas").withResolutions(resolutions).withValue(gasType.name()).build());
		}
		return hasPerms ? buildIntentInputWithPermissions("GasNearby", slots) : buildIntentInput("GasNearby", slots);
	}

	private static HandlerInput buildIntentInputWithPermissions(String intentName, Map<String, Slot> slots) {
		Intent intent = Intent.builder().withName(intentName).withSlots(slots).build();
		IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
		Permissions permissions = Permissions.builder().withConsentToken("consentToken").build();
		User user = User.builder().withPermissions(permissions).build();
		Session session = Session.builder().withSessionId(SESSION_ID).withUser(user).build();
		Device device = Device.builder().withDeviceId(DEVICE_ID).build();
		SystemState systemState = SystemState.builder().withUser(user).withApiAccessToken(API_ACCESS_TOKEN)
				.withApiEndpoint(API_ENDPOINT).withDevice(device).build();
		Context context = Context.builder().withSystem(systemState).build();
		RequestEnvelope envelope = RequestEnvelope.builder().withContext(context).withRequest(intentRequest)
				.withSession(session).build();
		return HandlerInput.builder().withRequestEnvelope(envelope).build();
	}

	private static Resolutions buildResolutions(String name, String id) {
		Resolutions resolutions = null;
		if (id != null) {
			Value value = Value.builder().withName(name).withId(id).build();
			ValueWrapper valueWrapper = ValueWrapper.builder().withValue(value).build();
			Resolution resolution = Resolution.builder().withValues(singletonList(valueWrapper)).build();
			resolutions = Resolutions.builder().withResolutionsPerAuthority(singletonList(resolution)).build();
		}
		return resolutions;
	}

	private static HandlerInput buildInput(Request speechletRequest) {
		User user = User.builder().build();
		Session session = Session.builder().withSessionId(SESSION_ID).withUser(user).build();
		Context context = Context.builder().build();
		RequestEnvelope envelope = RequestEnvelope.builder().withContext(context).withRequest(speechletRequest)
				.withSession(session).build();
		return HandlerInput.builder().withRequestEnvelope(envelope).build();
	}

	private InputBuilder() {
		// Not used.
	}

}
