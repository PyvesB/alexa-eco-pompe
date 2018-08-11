package io.github.pyvesb.alexaecopompe.data.unmarshal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

class LocalDateDeserializerTest {

	@Test
	void shouldDeserialiseLocalDateTimeStringsAsLocalDates() throws Exception {
		LocalDateDeserializer underTest = new LocalDateDeserializer();
		JsonParser jsonParser = new ObjectMapper().getFactory().createParser("\"2018-05-06 16:55:36\"");
		jsonParser.nextToken();	
		
		LocalDate actualLocalDate = underTest.deserialize(jsonParser, null);
		LocalDate expectedLocalDate = LocalDate.of(2018, Month.MAY, 06);
		
		assertEquals(expectedLocalDate, actualLocalDate);
	}

}
