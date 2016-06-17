package uk.gov.justice.services.generators.commons.validator;

import static java.util.Arrays.stream;

import org.raml.model.Raml;

/**
 * Used to compose raml validators
 */
public class CompositeRamlValidator implements RamlValidator {

    private final RamlValidator[] validators;

    public CompositeRamlValidator(final RamlValidator... validators) {
        this.validators = validators;
    }

    @Override
    public void validate(final Raml raml) {
        stream(validators).forEach(v -> v.validate(raml));
    }

}
