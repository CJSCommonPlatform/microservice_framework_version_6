package uk.gov.justice.services.common.converter;

import javax.faces.convert.ConverterException;
import javax.json.Json;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;

import static java.lang.String.format;

/**
 * Converts JsonValue to Json as a String.
 */
public class JsonValueToStringConverter implements Converter<JsonValue, String> {

    @Override
    public String convert(final JsonValue source) {
        try (final StringWriter stringWriter = new StringWriter();
             final JsonWriter writer = Json.createWriter(stringWriter)) {

            final JsonValue.ValueType valueType = source.getValueType();

            if (valueType == JsonValue.ValueType.OBJECT || valueType == JsonValue.ValueType.ARRAY) {
                writer.write((JsonStructure) source);
            } else {
                throw new IllegalArgumentException(format("Cannot convert %s to String.", valueType));
            }

            return stringWriter.getBuffer().toString();

        } catch (IOException e) {
            throw new ConverterException(format("Error while converting %s", source), e);
        }


    }

}
