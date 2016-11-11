package uk.gov.justice.services.core.json;

public interface JsonSchemaValidator {
    void validate(final String payload, final String name);
}
