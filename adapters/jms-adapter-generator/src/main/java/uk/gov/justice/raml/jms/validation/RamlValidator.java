package uk.gov.justice.raml.jms.validation;

import org.raml.model.Raml;

@FunctionalInterface
public interface RamlValidator {
    
    void validate(Raml raml);

}
