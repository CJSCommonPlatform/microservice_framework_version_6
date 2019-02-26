package uk.gov.justice.services.test.utils.common.validator;

import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.mapping.MediaType;

import java.util.Optional;

import javax.faces.bean.ApplicationScoped;

/**
 * Service for validating JSON payloads against a schema contained in a catalog.
 */
@ApplicationScoped
public class DummyJsonSchemaValidator implements JsonSchemaValidator {


    @Override
    public void validate(final String payload, final String actionName) {

    }

    @Override
    public void validate(final String payload, final String actionName, final Optional<MediaType> mediaType) {

    }
}
