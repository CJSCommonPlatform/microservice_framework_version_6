package uk.gov.justice.raml.common.validator;

import org.raml.model.Raml;

/**
 * Checks if @Raml object contains any resources
 */
public class ContainsResourcesRamlValidator implements RamlValidator {

    @Override
    public void validate(final Raml raml) {
        if (raml.getResources().isEmpty()) {
            throw new RamlValidationException("No resources specified");
        }
    }

}
