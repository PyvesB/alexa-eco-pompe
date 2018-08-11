package io.github.pyvesb.alexaecopompe.data.unmarshal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.github.pyvesb.alexaecopompe.domain.Price;

public abstract class GasStationMixIn {

	@SuppressWarnings("unused")
	@JsonCreator
	public GasStationMixIn(@JacksonXmlProperty(localName = "id", isAttribute = true) String id,
			@JacksonXmlProperty(localName = "latitude", isAttribute = true) float latitude,
			@JacksonXmlProperty(localName = "longitude", isAttribute = true) float longitude,
			@JacksonXmlProperty(localName = "cp", isAttribute = true) String postCode,
			@JacksonXmlProperty(localName = "ville") String town,
			@JacksonXmlProperty(localName = "adresse") String address,
			@JacksonXmlProperty(localName = "prix") List<Price> prices) {}

	@JacksonXmlProperty(localName = "prix")
	@JacksonXmlElementWrapper(useWrapping = false)
	public abstract List<Price> getPrices();

}
