package uk.gov.justice.services.common.json;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handly class for converting to and from Strings to pojo Objects.
 *
 * Can be used in tests as the internal ObjectMapper is not injected
 */
public class JsonParser {

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    /**
     * Converts from a json String to an Object
     *
     * @param json the json to parse
     * @param clazz the object to convert to
     * @param <T> the type of the object
     * @return one freshly parsed pojo
     * @throws RuntimeException if the parsing fails
     */
    public <T> T toObject(final String json, final Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert json '" + json + "' to " + clazz.getName(), e);
        }
    }

    /**
     * Converts from a pojo to a json String
     *
     * @param object the object to convert. Must be a jackson parsable object
     * @param <T> the object's type
     * @return the object as json
     * @throws RuntimeException if the object cannot be parsed to a json String
     */
    public <T> String fromObject(final T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert " + object.getClass().getName() + " to json", e);
        }
    }
}
