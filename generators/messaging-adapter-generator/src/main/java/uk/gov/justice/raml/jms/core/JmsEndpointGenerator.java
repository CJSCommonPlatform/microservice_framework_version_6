package uk.gov.justice.raml.jms.core;

import static org.raml.model.ActionType.POST;
import static uk.gov.justice.raml.jms.core.MediaTypesUtil.containsGeneralJsonMimeType;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.raml.jms.validator.BaseUriRamlValidator;
import uk.gov.justice.services.generators.commons.helper.MessagingAdapterBaseUri;
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
public class JmsEndpointGenerator implements Generator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsEndpointGenerator.class);
    private final MessageListenerCodeGenerator messageListenerCodeGenerator = new MessageListenerCodeGenerator();
    private final EventFilterCodeGenerator eventFilterCodeGenerator = new EventFilterCodeGenerator();


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

        validator.validate(raml);

        raml.getResources().values().stream()
                .filter(resource -> resource.getAction(POST) != null)
                .flatMap(resource -> generatedClassesFrom(raml, resource, configuration))
                .forEach(generatedClass -> writeClass(configuration, configuration.getBasePackageName(), generatedClass, LOGGER));
    }


    private Stream<? extends TypeSpec> generatedClassesFrom(final Raml raml, final Resource resource, final GeneratorConfig configuration) {
        final MessagingAdapterBaseUri baseUri = new MessagingAdapterBaseUri(raml.getBaseUri());
        final boolean listenToAllMessages = shouldListenToAllMessages(resource, baseUri);

        final TypeSpec messageListenerCode = messageListenerCodeGenerator.generatedCodeFor(resource, baseUri, listenToAllMessages, configuration);

        return shouldGenerateEventFilter(resource, baseUri)
                ? Stream.of(messageListenerCode, eventFilterCodeGenerator.generatedCodeFor(resource, baseUri))
                : Stream.of(messageListenerCode);
    }

    /*
    Two things here:
    1. If raml contains general json (application/json) then it means that we accept all messages, so no filter should be generated
    2. For event listeners we let all message through the listener and then filter them after they pass event buffer service.
    Therefore we need a generated filter based on raml.

    Note: Event buffer service contains functionality that puts messages in correct order basing on version number,
    therefore we need all messages with consecutive numbers there. Messages need to be in correct order in order to update the view correctly.
    */

    private boolean shouldGenerateEventFilter(final Resource resource, final MessagingAdapterBaseUri baseUri) {
        return EVENT_LISTENER.equals(baseUri.component()) && !containsGeneralJsonMimeType(resource.getActions());
    }

    private boolean shouldListenToAllMessages(final Resource resource, final MessagingAdapterBaseUri baseUri) {
        return EVENT_LISTENER.equals(baseUri.component()) || containsGeneralJsonMimeType(resource.getActions());
    }

}
