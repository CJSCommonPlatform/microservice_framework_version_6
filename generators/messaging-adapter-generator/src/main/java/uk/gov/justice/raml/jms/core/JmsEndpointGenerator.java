package uk.gov.justice.raml.jms.core;

import static org.raml.model.ActionType.POST;
import static uk.gov.justice.raml.jms.core.JmsEndPointGeneratorUtil.shouldGenerateEventFilter;
import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;

import uk.gov.justice.maven.generator.io.files.parser.core.Generator;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.raml.jms.interceptor.EventFilterInterceptorCodeGenerator;
import uk.gov.justice.raml.jms.interceptor.EventListenerInterceptorChainProviderCodeGenerator;
import uk.gov.justice.raml.jms.interceptor.EventValidationInterceptorCodeGenerator;
import uk.gov.justice.raml.jms.validator.BaseUriRamlValidator;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.generators.commons.helper.MessagingAdapterBaseUri;
import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;
import uk.gov.justice.services.generators.commons.mapping.MediaTypeToSchemaIdGenerator;
import uk.gov.justice.services.generators.commons.validator.CompositeRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsActionsRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;
import uk.gov.justice.services.generators.commons.validator.RequestContentTypeRamlValidator;

import java.util.stream.Stream;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates JMS endpoint classes out of RAML object
 */
public class JmsEndpointGenerator implements Generator<Raml> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsEndpointGenerator.class);
    private final MessageListenerCodeGenerator messageListenerCodeGenerator = new MessageListenerCodeGenerator();
    private final EventFilterCodeGenerator eventFilterCodeGenerator = new EventFilterCodeGenerator();
    private final MediaTypeToSchemaIdGenerator mediaTypeToSchemaIdGenerator = new MediaTypeToSchemaIdGenerator();
    private final EventFilterInterceptorCodeGenerator eventFilterInterceptorCodeGenerator = new EventFilterInterceptorCodeGenerator();
    private final EventValidationInterceptorCodeGenerator eventValidationInterceptorCodeGenerator = new EventValidationInterceptorCodeGenerator();
    private final EventListenerInterceptorChainProviderCodeGenerator eventListenerInterceptorChainProviderCodeGenerator = new EventListenerInterceptorChainProviderCodeGenerator();

    private final RamlValidator validator = new CompositeRamlValidator(
            new ContainsResourcesRamlValidator(),
            new ContainsActionsRamlValidator(),
            new RequestContentTypeRamlValidator(POST),
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

        final CommonGeneratorProperties commonGeneratorProperties = (CommonGeneratorProperties) configuration.getGeneratorProperties();
        final String basePackageName = configuration.getBasePackageName();

        validator.validate(raml);

        raml.getResources().values().stream()
                .filter(resource -> resource.getAction(POST) != null)
                .flatMap(resource -> generatedClassesFrom(raml, resource, commonGeneratorProperties, basePackageName))
                .forEach(generatedClass ->
                        writeClass(configuration, basePackageName, generatedClass, LOGGER)
                );

        mediaTypeToSchemaIdGenerator.generateMediaTypeToSchemaIdMapper(raml, configuration);
    }

    private Stream<TypeSpec> generatedClassesFrom(final Raml raml,
                                                  final Resource resource,
                                                  final CommonGeneratorProperties commonGeneratorProperties,
                                                  final String basePackageName) {

        final Stream.Builder<TypeSpec> streamBuilder = Stream.builder();

        final MessagingResourceUri resourceUri = new MessagingResourceUri(resource.getUri());
        final MessagingAdapterBaseUri baseUri = new MessagingAdapterBaseUri(raml.getBaseUri());
        final ClassNameFactory classNameFactory = new ClassNameFactory(baseUri, resourceUri, basePackageName);

        if (shouldGenerateEventFilter(resource, baseUri)) {

            streamBuilder
                    .add(eventFilterCodeGenerator.generate(resource, classNameFactory))
                    .add(eventFilterInterceptorCodeGenerator.generate(classNameFactory))
                    .add(eventValidationInterceptorCodeGenerator.generate(classNameFactory))
                    .add(eventListenerInterceptorChainProviderCodeGenerator.generate(
                            commonGeneratorProperties.getServiceComponent(),
                            classNameFactory));

        }

        streamBuilder.add(messageListenerCodeGenerator.generate(
                resource,
                baseUri,
                commonGeneratorProperties,
                classNameFactory));

        return streamBuilder.build();
    }
}
