package io.github.pyvesb.alexaecopompe.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NameProvider {

	private Map<String, String> idsToNames;

	public Optional<String> getById(String id) {
		if (idsToNames == null) {
			JavaType type = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, String.class);
			ObjectReader objectReader = new ObjectMapper().readerFor(type);
			try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("id_name_mapping.json")) {
				idsToNames = objectReader.readValue(inputStream);
			} catch (IOException e) {
				LogManager.getLogger(NameProvider.class).error("Failed to unmarshall gas station id name mapping", e);
				idsToNames = new HashMap<>();
			}
		}
		return Optional.ofNullable(idsToNames.get(id));
	}

}
