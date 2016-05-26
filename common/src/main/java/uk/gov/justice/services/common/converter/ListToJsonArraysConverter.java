package uk.gov.justice.services.common.converter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.justice.services.common.converter.exception.ConverterException;

/**
 * Converts a List of Type T to JsonArray
 */
public class ListToJsonArraysConverter<T> implements Converter<List<T>, JsonArray> {

	@Inject
	ObjectMapper mapper;

	@Inject
	StringToJsonObjectConverter stringToJsonObjectConverter;

	public JsonArray convert(final List<T> sourceList) {
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		if (sourceList == null) {
			throw new ConverterException(String.format("Failed to convert %s to JsonObject", sourceList));
		}
		sourceList.forEach((object) -> {
			try {
				jsonArrayBuilder.add(stringToJsonObjectConverter.convert(mapper.writeValueAsString(object)));
			} catch (IOException e) {
				throw new IllegalArgumentException(String.format("Error while converting %s toJsonObject", object), e);
			}
		});
		return jsonArrayBuilder.build();
	}

}
