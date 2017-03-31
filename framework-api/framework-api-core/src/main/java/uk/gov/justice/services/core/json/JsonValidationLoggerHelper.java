package uk.gov.justice.services.core.json;

import javax.json.JsonObject;

import org.everit.json.schema.ValidationException;

public interface JsonValidationLoggerHelper {

    String toValidationTrace(final ValidationException validationException);

    JsonObject toJsonObject(final ValidationException validationException);
}