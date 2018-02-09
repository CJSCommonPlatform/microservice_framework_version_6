package uk.gov.justice.services.core.json;

import javax.json.JsonObject;

import org.everit.json.schema.ValidationException;

/**
 * @deprecated Use injected jsonValidationLoggerHelper methods
 */
@Deprecated
public final class JsonValidationLogger {

    private JsonValidationLogger(){}

    /**
     * @deprecated Use jsonValidationLoggerHelper.toValidationTrace(JsonSchemaValidationException JsonSchemaValidationException)
     */
    @Deprecated
    public static String toValidationTrace(final JsonSchemaValidationException JsonSchemaValidationException) {
        return new DefaultJsonValidationLoggerHelper().toValidationTrace(JsonSchemaValidationException);
    }

    /**
     * @deprecated Use jsonValidationLoggerHelper.toJsonObject(JsonSchemaValidationException JsonSchemaValidationException)
     */
    @Deprecated
    public static JsonObject toJsonObject(final JsonSchemaValidationException JsonSchemaValidationException) {
        return new DefaultJsonValidationLoggerHelper().toJsonObject(JsonSchemaValidationException);
    }

    /**
     * @deprecated Use jsonValidationLoggerHelper.toValidationTrace(ValidationException validationException)
     */
    @Deprecated
    public static String toValidationTrace(final ValidationException validationException) {
        return new DefaultJsonValidationLoggerHelper().toValidationTrace(validationException);
    }

    /**
     * @deprecated Use jsonValidationLoggerHelper.toJsonObject(JsonSchemaValidationException JsonSchemaValidationException)
     */
    @Deprecated
    public static JsonObject toJsonObject(final ValidationException validationException) {
        return new DefaultJsonValidationLoggerHelper().toJsonObject(validationException);
    }

}
