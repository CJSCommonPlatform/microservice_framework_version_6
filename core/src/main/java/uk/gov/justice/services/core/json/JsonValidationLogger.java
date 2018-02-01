package uk.gov.justice.services.core.json;

import static java.lang.String.join;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

/**
 * @deprecated Use injected jsonValidationLoggerHelper methods
 */
public final class JsonValidationLogger {

    private JsonValidationLogger(){}

    /**
     * @deprecated Use jsonValidationLoggerHelper.toValidationTrace(JsonSchemaValidatonException jsonSchemaValidatonException)
     */
    public static String toValidationTrace(final JsonSchemaValidatonException jsonSchemaValidatonException) {
        return new DefaultJsonValidationLoggerHelper().toValidationTrace(jsonSchemaValidatonException);
    }

    /**
     * @deprecated Use jsonValidationLoggerHelper.toJsonObject(JsonSchemaValidatonException jsonSchemaValidatonException)
     */
    public static JsonObject toJsonObject(final JsonSchemaValidatonException jsonSchemaValidatonException) {
        return new DefaultJsonValidationLoggerHelper().toJsonObject(jsonSchemaValidatonException);
    }

    /**
     * @deprecated Use jsonValidationLoggerHelper.toValidationTrace(ValidationException validationException)
     */
    public static String toValidationTrace(final ValidationException validationException) {
        return new DefaultJsonValidationLoggerHelper().toValidationTrace(validationException);
    }

    /**
     * @deprecated Use jsonValidationLoggerHelper.toJsonObject(JsonSchemaValidatonException jsonSchemaValidatonException)
     */
    public static JsonObject toJsonObject(final ValidationException validationException) {
        return new DefaultJsonValidationLoggerHelper().toJsonObject(validationException);
    }

}
