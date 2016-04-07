package uk.gov.justice.raml.common.validator;

import org.raml.model.Raml;

@FunctionalInterface
public interface RamlValidator {
    
    void validate(Raml raml);

}
