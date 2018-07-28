package io.github.pyvesb.alexaecopompe.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;


class NameProviderTest {
	
	private final NameProvider underTest = new NameProvider();

	@Test
	void shouldGetGasStationNameById() {
		assertEquals(Optional.of("Pyves Gas"), underTest.getById("1"));
	}
	
	@Test
	void shouldReturnEmptyOptionalIfIdNotFound() {
		assertEquals(Optional.empty(), underTest.getById("2"));
	}

}
