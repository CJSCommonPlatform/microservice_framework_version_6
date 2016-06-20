package uk.gov.justice.services.generators.commons.validator;

import org.raml.model.Raml;

@FunctionalInterface
public interface RamlValidator {

    void validate(Raml raml);

}
