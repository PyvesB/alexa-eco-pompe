package utils;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.AskForPermissionsConsentCard;
import com.amazon.ask.model.ui.SimpleCard;
import com.amazon.ask.model.ui.SsmlOutputSpeech;

public class ResponseAssertions {

	public static void assertSpeech(String text, Response response) {
		SsmlOutputSpeech ssmlOutputSpeech = (SsmlOutputSpeech) response.getOutputSpeech();
		assertNotNull(ssmlOutputSpeech, "No output speech was found in the reponse");
		assertEquals("<speak>" + text + "</speak>", ssmlOutputSpeech.getSsml());
	}

	public static void assertCard(String content, Response response) {
		SimpleCard card = (SimpleCard) response.getCard();
		assertNotNull(card, "No card was found in the reponse");
		assertEquals("Éco Pompe", card.getTitle());
		assertEquals(content, card.getContent());
	}

	public static void assertCardWithPermissions(Response response) {
		AskForPermissionsConsentCard card = (AskForPermissionsConsentCard) response.getCard();
		assertNotNull(card, "No card was found in the reponse");
		assertEquals(singletonList("read::alexa:device:all:address"), card.getPermissions());
	}
	
	private ResponseAssertions() {
		// Not used.
	}

}
