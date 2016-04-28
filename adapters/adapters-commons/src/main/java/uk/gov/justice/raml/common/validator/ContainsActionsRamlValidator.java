package uk.gov.justice.raml.common.validator;

import org.raml.model.Resource;

/**
 * Checks if all resources defined in @Raml contain actions
 */
public class ContainsActionsRamlValidator extends AbstractResourceRamlValidator {

    private static final String ERROR_MSG = "No actions to process";

    @Override
    protected void validate(final Resource resource) {
        if (resource.getActions().isEmpty()) {
            throw new RamlValidationException(ERROR_MSG);
        }
    }
}
