package uk.gov.justice.services.core.json;

import javax.json.JsonObject;

import org.everit.json.schema.ValidationException;

public class DefaultJsonValidationLoggerHelper implements JsonValidationLoggerHelper {

    @Override
    public String toValidationTrace(final ValidationException validationException) {
        return JsonValidationLogger.toValidationTrace(validationException);
    }

    @Override
    public JsonObject toJsonObject(final ValidationException validationException) {
        return JsonValidationLogger.toJsonObject(validationException);
    }
}