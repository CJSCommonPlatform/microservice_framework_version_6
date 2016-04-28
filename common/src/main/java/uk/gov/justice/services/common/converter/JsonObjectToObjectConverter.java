package uk.gov.justice.services.common.converter;

import uk.gov.justice.services.common.converter.exception.ConverterException;

import java.io.IOException;

import javax.inject.Inject;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts JsonObject to the given Pojo type.
 */
public class JsonObjectToObjectConverter implements TypedConverter<JsonObject, Object> {

    @Inject
    ObjectMapper mapper;

    @Override
    public <R> R convert(final JsonObject source, final Class<R> clazz) {
        try {
            final R object = mapper.readValue(mapper.writeValueAsString(source), clazz);

            if (object == null) {
                throw new ConverterException(String.format("Failed to convert %s to Object", source));
            }

            return object;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Error while converting %s to JsonObject", source), e);
        }
    }
}
