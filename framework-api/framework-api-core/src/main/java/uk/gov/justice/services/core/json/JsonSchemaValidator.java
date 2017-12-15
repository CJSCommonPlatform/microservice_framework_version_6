package uk.gov.justice.services.core.json;

import uk.gov.justice.services.core.mapping.MediaType;

public interface JsonSchemaValidator {

    void validate(final String payload, final MediaType mediaType);
}
