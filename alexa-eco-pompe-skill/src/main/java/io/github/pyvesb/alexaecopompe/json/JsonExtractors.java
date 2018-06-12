package io.github.pyvesb.alexaecopompe.json;

import java.util.function.Function;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

import io.github.pyvesb.alexaecopompe.geography.Position;

public class JsonExtractors {

	public static Function<JsonNode, String> nameExtractor(String namePath) {
		JsonPointer namePointer = JsonPointer.compile(namePath);
		return jsonNode -> {
			JsonNode nameNode = jsonNode.at(namePointer);
			return nameNode.isMissingNode() ? null : nameNode.asText();
		};
	}

	public static Function<JsonNode, Position> positionExtractor(String latPath, String lonPath) {
		JsonPointer latPointer = JsonPointer.compile(latPath);
		JsonPointer lonPointer = JsonPointer.compile(lonPath);
		return jsonNode -> {
			JsonNode latNode = jsonNode.at(latPointer);
			JsonNode lonNode = jsonNode.at(lonPointer);
			boolean validData = NumberUtils.isParsable(latNode.asText()) && NumberUtils.isParsable(lonNode.asText());
			return validData ? new Position(Float.parseFloat(latNode.asText()), Float.parseFloat(lonNode.asText())) : null;
		};
	}

	private JsonExtractors() {
		// Not called.
	}

}
