package uk.gov.justice.services.core.json;

import javax.json.JsonObject;

public class DefaultJsonValidationLoggerHelper implements JsonValidationLoggerHelper {

    @Override
    public String toValidationTrace(final JsonSchemaValidatonException jsonSchemaValidatonException) {
        return JsonValidationLogger.toValidationTrace(jsonSchemaValidatonException);
    }

    @Override
    public JsonObject toJsonObject(final JsonSchemaValidatonException jsonSchemaValidatonException) {
        return JsonValidationLogger.toJsonObject(jsonSchemaValidatonException);
    }
}