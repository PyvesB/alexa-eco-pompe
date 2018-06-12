package utils;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.AskForPermissionsConsentCard;
import com.amazon.ask.model.ui.SimpleCard;
import com.amazon.ask.model.ui.SsmlOutputSpeech;

public class ResponseAssertions {

	public static void assertSpeech(String text, Response response) {
		assertEquals("<speak>" + text + "</speak>", ((SsmlOutputSpeech) response.getOutputSpeech()).getSsml());
	}

	public static void assertCard(String content, Response response) {
		SimpleCard card = (SimpleCard) response.getCard();
		assertEquals("Éco Pompe", card.getTitle());
		assertEquals(content, card.getContent());
	}

	public static void assertCardWithPermissions(Response response) {
		AskForPermissionsConsentCard card = (AskForPermissionsConsentCard) response.getCard();
		assertEquals(singletonList("read::alexa:device:all:address"), card.getPermissions());
	}
	
	private ResponseAssertions() {
		// Not used.
	}

}
