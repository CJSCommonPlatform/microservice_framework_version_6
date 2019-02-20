package uk.gov.justice.services.core.json;

import static java.util.Arrays.asList;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.core.mapping.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

/**
 * Service for validating JSON payloads against a schema contained in a catalog.
 */
@ApplicationScoped
public class BackwardsCompatibleJsonSchemaValidator implements JsonSchemaValidator {

    @Inject
    SchemaCatalogAwareJsonSchemaValidator schemaCatalogAwareJsonSchemaValidator;

    @Inject
    FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator;

    @Inject
    @Value(key = "schema.validation.action.whitelist")
    String whitelistActionName;

    private final List<String> actionNames = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        final String[] actions = whitelistActionName.split(",");
        actionNames.addAll(asList(actions));
    }

    /**
     * Validate a JSON payload by falling back to checking for schemas on the class path.
     *
     * @param envelopeJson the payload to validate
     * @param actionName   the name of the command
     */
    @Override
    public void validate(final String envelopeJson, final String actionName) {
        fileBasedJsonSchemaValidator.validateWithoutSchemaCatalog(envelopeJson, actionName);
    }

    /**
     * Validate a JSON payload against a schema contained in the schema catalog for the given message
     * type name. If the JSON contains metadata, this is removed first.  If no schema for the media type
     * can be found then it falls back to checking for schemas on the class path. If media type is present
     * in whitelist it will not be validate.
     *
     * @param envelopeJson the payload to validate
     * @param actionName   the name of the command
     * @param mediaType    the message type (Optional)
     */
    @Override
    public void validate(final String envelopeJson, final String actionName, final Optional<MediaType> mediaType) {

        if (isValidationRequiredFor(actionName)) {
            if (mediaType.isPresent()) {
                schemaCatalogAwareJsonSchemaValidator.validate(envelopeJson, actionName, mediaType);
            } else {
                validate(envelopeJson, actionName);
            }
        }
    }

    private boolean isValidationRequiredFor(final String actionName) {
        return !actionNames.contains(actionName);
    }
}
