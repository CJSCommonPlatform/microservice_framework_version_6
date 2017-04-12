package uk.gov.justice.services.generators.commons.validator;


import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.Collection;

import org.raml.model.ActionType;
import org.raml.model.Resource;

public class SupportedActionTypesRamlValidator extends AbstractResourceRamlValidator {
    private final Collection<ActionType> supportedActionTypes;

    public SupportedActionTypesRamlValidator(final ActionType... supportedActionTypes) {
        this.supportedActionTypes = asList(supportedActionTypes);
    }

    @Override
    protected void validate(final Resource resource) {
        resource.getActions().keySet()
                .forEach(actionType -> {
                            if (!this.supportedActionTypes.contains(actionType)) {
                                throw new RamlValidationException(format("Http action type not supported: %s", actionType));
                            }
                        }
                );

    }
}
