package uk.gov.justice.services.common.converter.jackson.jsr353;

import javax.json.JsonValue;

import com.fasterxml.jackson.datatype.jsr353.JSR353Module;

/**
 * Customised version of the JSR353Module that excludes null values.
 */
public class InclusionAwareJSR353Module extends JSR353Module {

    private static final long serialVersionUID = -1112006287109681036L;

    public InclusionAwareJSR353Module() {
        addSerializer(JsonValue.class, new InclusionAwareJsonValueSerializer());
    }
}
