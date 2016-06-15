package uk.gov.justice.raml.jms.core;

import uk.gov.justice.raml.common.validator.CompositeRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsActionsRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.raml.common.validator.RamlValidator;
import uk.gov.justice.raml.common.validator.RequestContentTypeRamlValidator;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.raml.jms.uri.BaseUri;
import uk.gov.justice.raml.jms.validator.BaseUriRamlValidator;
import uk.gov.justice.raml.jms.validator.ResourceUriRamlValidator;

import java.io.IOException;
import java.util.Collection;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.raml.model.Resource;

/**
 * Generates JMS endpoint classes out of RAML object
 */
public class JmsEndpointGenerator implements Generator {

    private final RamlValidator validator = new CompositeRamlValidator(
            new ResourceUriRamlValidator(),
            new ContainsResourcesRamlValidator(),
            new ContainsActionsRamlValidator(),
            new RequestContentTypeRamlValidator(),
            new BaseUriRamlValidator()
    );

    /**
     * Generates JMS endpoint classes from a RAML document.
     * @param raml the RAML document
     * @param configuration contains package of generated sources, as well as source and destination folders
     */
    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {

        validator.validate(raml);

        final Collection<Resource> ramlResourceModels = raml.getResources().values();
        final BaseUri baseUri = new BaseUri(raml.getBaseUri());

        final MessageListenerCodeGenerator messageListenerCodeGenerator = new MessageListenerCodeGenerator();

        ramlResourceModels.stream()
                .map(resource -> messageListenerCodeGenerator.generateFor(resource, baseUri))
                .forEach(typeSpec -> writeClassToFile(typeSpec, configuration));
    }

    private void writeClassToFile(final TypeSpec typeSpec, final GeneratorConfig configuration) {
        try {
            JavaFile.builder(configuration.getBasePackageName(), typeSpec)
                    .build()
                    .writeTo(configuration.getOutputDirectory());

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
