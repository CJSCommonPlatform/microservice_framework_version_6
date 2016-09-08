package uk.gov.justice.services.common.json;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    public <T> T toObject(final String json, final Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert " + clazz.getName() + " to json: " + json, e);
        }
    }

    public <T> String fromObject(final T object) {

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert " + object.getClass().getName() + " to json", e);
        }
    }
}
