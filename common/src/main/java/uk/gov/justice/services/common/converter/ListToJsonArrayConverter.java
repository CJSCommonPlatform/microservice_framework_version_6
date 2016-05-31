package uk.gov.justice.services.common.converter;

import static javax.json.Json.createArrayBuilder;
import static java.lang.String.format;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.justice.services.common.converter.exception.ConverterException;

/**
 * Converts a List of Type <T> to JsonArray
 *
 * @param <T> the type of objects in the List
 */
public class ListToJsonArrayConverter<T> implements Converter<List<T>, JsonArray> {
	@Inject
	ObjectMapper mapper;

	@Inject
	StringToJsonObjectConverter stringToJsonObjectConverter;

	public JsonArray convert(final List<T> sourceList) {
		JsonArrayBuilder jsonArrayBuilder = createArrayBuilder();
		if (sourceList == null) {
			throw new ConverterException(format("Failed to convert %s to JsonArray", sourceList));
		}
		sourceList.forEach((object) -> {
			try {
				jsonArrayBuilder.add(stringToJsonObjectConverter.convert(mapper.writeValueAsString(object)));
			} catch (IOException e) {
				throw new IllegalArgumentException(format("Error while converting %s JsonArray", object), e);
			}
		});
		return jsonArrayBuilder.build();
	}

}
