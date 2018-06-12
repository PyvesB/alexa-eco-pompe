package utils;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;

public class TestFactoryHelper {

	public static String buildDynamicDisplayName(HandlerInput input) {
		Map<String, String> nameValueMap = ((IntentRequest) input.getRequestEnvelope().getRequest()).getIntent().getSlots()
				.entrySet().stream().collect(toMap(e -> e.getKey(), e -> e.getValue().getValue()));
		return "Intent=" + getIntentName(input) + " slots=" + nameValueMap;
	}

	public static String getIntentName(HandlerInput input) {
		return ((IntentRequest) input.getRequestEnvelope().getRequest()).getIntent().getName();
	}

	private TestFactoryHelper() {
		// Not used.
	}

}
