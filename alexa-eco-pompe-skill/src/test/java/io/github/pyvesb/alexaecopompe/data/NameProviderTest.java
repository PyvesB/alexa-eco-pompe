package io.github.pyvesb.alexaecopompe.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.pyvesb.alexaecopompe.data.NameProvider;


class NameProviderTest {
	
	private final NameProvider underTest = new NameProvider();

	@Test
	void shouldGetGasStationNameById() {
		Optional<String> name = underTest.getById("1");
		
		assertTrue(name.isPresent());
		assertEquals("Pyves Gas", name.get());
	}
	
	@Test
	void shouldReturnEmptyOptionalIfIdNotFound() {
		assertFalse(underTest.getById("2").isPresent());
	}

}
