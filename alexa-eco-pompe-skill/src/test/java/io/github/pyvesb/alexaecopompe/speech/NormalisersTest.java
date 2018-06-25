package io.github.pyvesb.alexaecopompe.speech;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import io.github.pyvesb.alexaecopompe.speech.Normalisers;

class NormalisersTest {

	@ParameterizedTest
	@CsvFileSource(resources = { "/town_normalisation.csv" })
	void shouldNormaliseTownNames(String input, String normalisation) {
		assertEquals(normalisation, Normalisers.normaliseTown(input));
	}
	
	@ParameterizedTest
	@CsvFileSource(resources = { "/address_normalisation.csv" })
	void shouldNormaliseGasStationAddresses(String input, String normalisation) {
		assertEquals(normalisation, Normalisers.normaliseAddress(input));
	}
	
}
