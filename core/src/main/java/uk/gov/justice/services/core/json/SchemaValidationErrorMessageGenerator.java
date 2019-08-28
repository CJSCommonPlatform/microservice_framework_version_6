package uk.gov.justice.services.core.json;

import static java.lang.String.format;

import java.util.List;

import org.everit.json.schema.ValidationException;

public class SchemaValidationErrorMessageGenerator {

    private static final String ERROR_MASSAGE_FORMAT = "Json validation failed with %d violation(s): %s. Errors: %s. Violated schema id: '%s'. Location: '%s'";

    public String generateErrorMessage(final ValidationException e) {

        final int violationCount = e.getViolationCount();
        final String errorMessage = e.getErrorMessage();
        final List<String> allMessages = e.getAllMessages();
        final String id = e.getViolatedSchema().getId();
        final String schemaLocation = e.getSchemaLocation();

        return format(ERROR_MASSAGE_FORMAT, violationCount, errorMessage, allMessages, id, schemaLocation);
    }
}
