package uk.gov.justice.services.common.converter;

import static javax.json.JsonValue.NULL;

import uk.gov.justice.services.common.converter.exception.ConverterException;

import java.io.IOException;

import javax.inject.Inject;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts a Pojo to a JsonValue
 */
public class ObjectToJsonValueConverter implements Converter<Object, JsonValue> {

    @Inject
    ObjectMapper mapper;

    @Override
    public JsonValue convert(final Object source) {
        try {
            if (source == null) {
                return NULL;
            }

            final JsonValue jsonValue = mapper.readValue(mapper.writeValueAsString(source), JsonValue.class);

            if (jsonValue == null) {
                throw new ConverterException(String.format("Failed to convert %s to JsonValue", source));
            }

            return jsonValue;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Error while converting %s to JsonValue", source), e);
        }
    }

}
