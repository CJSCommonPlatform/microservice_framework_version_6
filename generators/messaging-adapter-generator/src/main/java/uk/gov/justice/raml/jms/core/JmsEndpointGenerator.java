package uk.gov.justice.raml.jms.core;

import static org.raml.model.ActionType.POST;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.raml.jms.uri.BaseUri;
import uk.gov.justice.raml.jms.validator.BaseUriRamlValidator;
import uk.gov.justice.raml.jms.validator.ResourceUriRamlValidator;
import uk.gov.justice.services.generators.commons.validator.CompositeRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsActionsRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;
import uk.gov.justice.services.generators.commons.validator.RequestContentTypeRamlValidator;

import java.io.IOException;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;

/**
 * Generates JMS endpoint classes out of RAML object
 */
public class JmsEndpointGenerator implements Generator {
    private final MessageListenerCodeGenerator messageListenerCodeGenerator = new MessageListenerCodeGenerator();

    private final RamlValidator validator = new CompositeRamlValidator(
            new ResourceUriRamlValidator(),
            new ContainsResourcesRamlValidator(),
            new ContainsActionsRamlValidator(),
            new RequestContentTypeRamlValidator(),
            new BaseUriRamlValidator()
    );

    /**
     * Generates JMS endpoint classes from a RAML document.
     *
     * @param raml          the RAML document
     * @param configuration contains package of generated sources, as well as source and destination
     *                      folders
     */
    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {

        validator.validate(raml);

        raml.getResources().values().stream()
                .filter(resource -> resource.getAction(POST) != null)
                .map(resource -> messageListenerCodeGenerator.generateFor(resource, new BaseUri(raml.getBaseUri())))
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
