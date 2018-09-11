package io.github.pyvesb.alexaecopompe.handlers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.InputBuilder.buildLaunchInput;
import static utils.ResponseAssertions.assertSpeech;

import org.junit.jupiter.api.Test;

import com.amazon.ask.model.Response;

import utils.InputBuilder;
import utils.MissingResponse;

class LaunchRequestHandlerTest {

	private final LaunchRequestHandler underTest = new LaunchRequestHandler();

	@Test
	void shouldHandleLaunchRequests() {
		assertTrue(underTest.canHandle(buildLaunchInput()));
	}

	@Test
	void shouldNotHandleOtherRequests() {
		assertFalse(underTest.canHandle(InputBuilder.buildIntentInput("GasTown")));
	}

	@Test
	void shouldReturnLaunchResponse() {
		Response resp = underTest.handle(buildLaunchInput()).orElseThrow(MissingResponse::new);

		assertFalse(resp.getShouldEndSession());
		assertSpeech("Bienvenue ! Je peux trouver le carburant le moins cher par ville, par département ou près de "
				+ "chez vous. Dîtes \"aide\" pour obtenir les instructions, ou \"stop\" pour quitter Éco Pompe.", resp);
		assertNull(resp.getCard());
	}

}
