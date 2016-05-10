package uk.gov.justice.services.adapters.rest.generator.strategy;

import uk.gov.justice.raml.common.validator.CompositeRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsActionsRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.raml.common.validator.RamlValidator;
import uk.gov.justice.raml.common.validator.RequestContentTypeRamlValidator;
import uk.gov.justice.services.adapters.rest.generator.MediaTypeMapperGenerator;
import uk.gov.justice.services.adapters.rest.validator.BaseUriRamlValidator;
import uk.gov.justice.services.adapters.rest.validator.ResponseContentTypeRamlValidator;

import java.util.List;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;

/**
 * Media type generation strategy to support backwards compatibility.
 */
public class AdapterMediaTypeGeneration implements AdapterGenerationStrategy {

    /**
     * Creates the {@link RamlValidator} for the MediaType mapper strategy
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
                new BaseUriRamlValidator());
    }

    /**
     * Creates and returns a list of TypeSpec that represent the MediaType mapper classes that are
     * created for the generation strategy.
     *
     * @param raml the RAML to parse to create mappings
     * @return the list of TypeSpec that represent the mappings
     */
    @Override
    public List<TypeSpec> generateFor(final Raml raml) {
        return new MediaTypeMapperGenerator().generateFor(raml);
    }

}
