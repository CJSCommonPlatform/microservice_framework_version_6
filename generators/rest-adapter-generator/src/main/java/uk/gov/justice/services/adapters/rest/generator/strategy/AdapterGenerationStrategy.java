package uk.gov.justice.services.adapters.rest.generator.strategy;


import uk.gov.justice.services.generators.commons.validator.RamlValidator;

import java.util.List;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;

public interface AdapterGenerationStrategy {

    /**
     * Creates the {@link RamlValidator} for this generation strategy
     *
     * @return the {@link RamlValidator}
     */
    RamlValidator validator();

    /**
     * Creates and returns a list of TypeSpec that represent the Mapping classes that are created
     * for the generation strategy.
     *
     * @param raml the RAML to parse to create mappings
     * @return the list of TypeSpec that represent the mappings
     */
    List<TypeSpec> generateFor(final Raml raml);

}
