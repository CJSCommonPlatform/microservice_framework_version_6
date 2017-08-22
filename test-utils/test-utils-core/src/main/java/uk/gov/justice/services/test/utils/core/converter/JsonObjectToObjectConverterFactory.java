package uk.gov.justice.services.test.utils.core.converter;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.lang.reflect.Field;

public class JsonObjectToObjectConverterFactory {

    public static JsonObjectToObjectConverter createJsonObjectToObjectConverter() {
        JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
        Field mapperField = retrieveObjectMapperField(jsonObjectToObjectConverter);
        setRealObjectMapper(jsonObjectToObjectConverter, mapperField);

        return jsonObjectToObjectConverter;
    }

    private static void setRealObjectMapper(JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                            Field mapperField) {
        try {
            mapperField.setAccessible(true);
            mapperField.set(jsonObjectToObjectConverter, new ObjectMapperProducer().objectMapper());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field retrieveObjectMapperField(JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        Class<? extends JsonObjectToObjectConverter> converterClass = jsonObjectToObjectConverter.getClass();
        Field[] declaredFields = converterClass.getDeclaredFields();
        Field mapperField = null;

        for (Field field : declaredFields) {
            if (field.getType().getName().equals("com.fasterxml.jackson.databind.ObjectMapper")) {
                mapperField = field;
                break;
            }
        }
        return mapperField;
    }

}