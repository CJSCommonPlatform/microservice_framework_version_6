package uk.gov.justice.services.core.json;

import javax.json.JsonObject;

public interface JsonValidationLoggerHelper {

    String toValidationTrace(final JsonSchemaValidatonException validationException);

    JsonObject toJsonObject(final JsonSchemaValidatonException validationException);
}