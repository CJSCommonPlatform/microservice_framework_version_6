package uk.gov.justice.services.common.converter.jackson.jsr353;

import static javax.json.JsonValue.NULL;

import javax.json.JsonArray;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Helper functions for the inclusion-aware seriaizer.
 */
public final class JsonIncludes {

    private JsonIncludes() {
    }

    public static boolean includeField(final JsonValue jsonValue, final Include include) {
        switch(include) {
            case NON_NULL:
            case NON_ABSENT:
                return !NULL.equals(jsonValue);
            case NON_EMPTY:
                return !isJsonValueEmpty(jsonValue);
            case ALWAYS:
            case NON_DEFAULT:
            case USE_DEFAULTS:
            default:
                return true;
        }
    }

    private static boolean isJsonValueEmpty(final JsonValue value) {
        switch(value.getValueType()) {
            case ARRAY:
                return ((JsonArray) value).isEmpty();
            case STRING:
                return ((JsonString) value).getString().isEmpty();
            case NULL:
                return true;
            default:
                return false;
        }
    }
}
