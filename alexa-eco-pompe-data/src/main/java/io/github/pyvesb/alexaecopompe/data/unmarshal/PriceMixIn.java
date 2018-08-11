package io.github.pyvesb.alexaecopompe.data.unmarshal;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.github.pyvesb.alexaecopompe.domain.GasType;

public abstract class PriceMixIn {

	@JacksonXmlProperty(localName = "id", isAttribute = true)
	private GasType type;
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JacksonXmlProperty(localName = "maj", isAttribute = true)
	private LocalDate updated;
	@JacksonXmlProperty(localName = "valeur", isAttribute = true)
	private float value;

}
