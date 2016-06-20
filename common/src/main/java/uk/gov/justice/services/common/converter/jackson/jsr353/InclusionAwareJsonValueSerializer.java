package uk.gov.justice.services.common.converter.jackson.jsr353;

import static uk.gov.justice.services.common.converter.jackson.jsr353.JsonIncludes.includeField;

import java.io.IOException;
import java.util.Map.Entry;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr353.JsonValueSerializer;

/**
 * Customised version of the JSR353 JsonValueSerializer that excludes null values.
 */
public class InclusionAwareJsonValueSerializer extends JsonValueSerializer {

    private static final long serialVersionUID = -1774633256022315530L;

    @Override
    protected void serializeObjectContents(final JsonObject jsonObject,
                                           final JsonGenerator generator,
                                           final SerializerProvider provider) throws IOException {
        if (!jsonObject.isEmpty()) {
            for (final Entry<String, JsonValue> entry : jsonObject.entrySet()) {
                if (includeField(entry.getValue(), provider.getConfig().getSerializationInclusion())) {
                    generator.writeFieldName(entry.getKey());
                    serialize(entry.getValue(), generator, provider);
                }
            }
        }
    }
}
