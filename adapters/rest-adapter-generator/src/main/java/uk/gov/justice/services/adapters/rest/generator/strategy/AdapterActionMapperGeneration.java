package uk.gov.justice.services.adapters.rest.generator.strategy;

import uk.gov.justice.raml.common.validator.ActionMappingRamlValidator;
import uk.gov.justice.raml.common.validator.CompositeRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsActionsRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.raml.common.validator.RamlValidator;
import uk.gov.justice.raml.common.validator.RequestContentTypeRamlValidator;
import uk.gov.justice.services.adapters.rest.generator.ActionMapperGenerator;
import uk.gov.justice.services.adapters.rest.validator.BaseUriRamlValidator;
import uk.gov.justice.services.adapters.rest.validator.ResponseContentTypeRamlValidator;

import java.util.List;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;

/**
 * Action mapping generation strategy to support Action Mapping.
 */
public class AdapterActionMapperGeneration implements AdapterGenerationStrategy {

    /**
     * Creates the {@link RamlValidator} for the ActionMapper strategy
     *
     * @return the {@link RamlValidator}
     */
    @Override
    public RamlValidator validator() {
        return new CompositeRamlValidator(
                new ContainsResourcesRamlValidator(),
                new ContainsActionsRamlValidator(),
                new RequestContentTypeRamlValidator(),
                new ResponseContentTypeRamlValidator(),
                new BaseUriRamlValidator(),
                new ActionMappingRamlValidator());
    }

    /**
     * Creates and returns a list of TypeSpec that represent the ActionMapper classes that are
     * created for the generation strategy.
     *
     * @param raml the RAML to parse to create mappings
     * @return the list of TypeSpec that represent the mappings
     */
    @Override
    public List<TypeSpec> generateFor(final Raml raml) {
        return new ActionMapperGenerator().generateFor(raml);
    }

}
