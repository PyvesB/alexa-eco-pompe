package io.github.pyvesb.alexaecopompe.data.unmarshal;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

final class LocalDateDeserializer extends StdDeserializer<LocalDate> {

	private static final long serialVersionUID = 1L;

	LocalDateDeserializer() {
		super(LocalDate.class);
	}

	@Override
	public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		return LocalDate.parse(p.getValueAsString().subSequence(0, 10));
	}

}
