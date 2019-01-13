package io.github.pyvesb.alexaecopompe.handlers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.InputBuilder.buildLaunchInput;
import static utils.ResponseAssertions.assertSpeech;

import org.junit.jupiter.api.Test;

import com.amazon.ask.model.Response;

import utils.MissingResponse;

class CustomLaunchRequestHandlerTest {

	private final CustomLaunchRequestHandler underTest = new CustomLaunchRequestHandler();

	@Test
	void shouldHandleLaunchRequests() {
		assertTrue(underTest.canHandle(buildLaunchInput()));
	}

	@Test
	void shouldReturnLaunchResponse() {
		Response resp = underTest.handle(buildLaunchInput()).orElseThrow(MissingResponse::new);

		assertFalse(resp.getShouldEndSession());
		assertSpeech("Bienvenue ! Je peux trouver le carburant le moins cher par ville, par département ou près de "
				+ "vous. Dîtes \"aide\" pour obtenir les instructions, ou \"stop\" pour quitter Éco Pompe.", resp);
		assertNull(resp.getCard());
	}

}
