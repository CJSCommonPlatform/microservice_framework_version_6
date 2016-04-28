package uk.gov.justice.services.common.converter;

import uk.gov.justice.services.common.converter.exception.ConverterException;

import java.io.IOException;

import javax.inject.Inject;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts a Pojo to a JsonObject
 */
public class ObjectToJsonObjectConverter implements Converter<Object, JsonObject> {

    @Inject
    ObjectMapper mapper;

    @Override
    public JsonObject convert(final Object source) {
        try {
            final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(source), JsonObject.class);

            if (jsonObject == null) {
                throw new ConverterException(String.format("Failed to convert %s to JsonObject", source));
            }

            return jsonObject;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Error while converting %s toJsonObject", source), e);
        }
    }

}
