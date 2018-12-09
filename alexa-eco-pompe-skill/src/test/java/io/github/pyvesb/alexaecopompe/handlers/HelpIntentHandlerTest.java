package io.github.pyvesb.alexaecopompe.handlers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.InputBuilder.buildIntentInput;
import static utils.ResponseAssertions.assertCard;
import static utils.ResponseAssertions.assertSpeech;

import org.junit.jupiter.api.Test;

import com.amazon.ask.model.Response;

import utils.MissingResponse;

class HelpIntentHandlerTest {

	private final HelpIntentHandler underTest = new HelpIntentHandler();

	@Test
	void shouldHandleIntentRequestsWithHelpIntentName() {
		assertTrue(underTest.canHandle(buildIntentInput("AMAZON.HelpIntent")));
	}

	@Test
	void shouldNotHandleIntentRequestsWithDifferentName() {
		assertFalse(underTest.canHandle(buildIntentInput("GasTown")));
	}

	@Test
	void shouldReturnHelpResponse() {
		Response resp = underTest.handle(buildIntentInput("AMAZON.HelpIntent")).orElseThrow(MissingResponse::new);

		assertFalse(resp.getShouldEndSession());
		assertSpeech("Spécifiez un carburant suivi d'une ville, d'un département ou d'une distance. Par exemple : \"le sans "
				+ "plomb 98 à Lyon\", \"le gazole dans la Creuse\", ou \"l'E10 à moins de 10 kilomètres\".", resp);
		assertCard("Exemples:\n\"le sans plomb 98 à Lyon\"\n\"le gazole dans la Creuse\"\n\"l'E10 à moins de 10 kilomètres\""
				+ "\nCarburants: gazole, sans plomb 95, sans plomb 98, E10, E85 ou GPL.", resp);
	}

}
