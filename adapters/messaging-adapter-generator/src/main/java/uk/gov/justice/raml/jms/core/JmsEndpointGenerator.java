package uk.gov.justice.raml.jms.core;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.lang.StringUtils.capitalize;

import uk.gov.justice.raml.common.validator.CompositeRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsActionsRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.raml.common.validator.RamlValidator;
import uk.gov.justice.raml.common.validator.RequestContentTypeRamlValidator;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.raml.jms.uri.BaseUri;
import uk.gov.justice.raml.jms.uri.ResourceUri;
import uk.gov.justice.raml.jms.validator.BaseUriRamlValidator;
import uk.gov.justice.raml.jms.validator.ResourceUriRamlValidator;
import uk.gov.justice.services.core.annotation.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.Topic;

import org.apache.commons.lang3.tuple.Pair;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.stringtemplate.v4.ST;

/**
 * Generates JMS endpoint classes out of RAML object
 */
public class JmsEndpointGenerator implements Generator {

    private static final String UTF_8 = "UTF-8";
    private static final String JMS_TEMPLATE_RESOURCE = "JmsListenerTemplate.tpm";
    private static final String TEMPLATE_LOADING_ERROR = format("Failed to load template resource %s", JMS_TEMPLATE_RESOURCE);
    private static final String OUTPUT_FILE_GENERATION_ERROR = "Failed to create output file for %s";
    private static final String FILENAME_POSTFIX = "JmsListener.java";
    private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("(application/vnd.)(\\S+)(\\+\\S+)");
    private static final String PACKAGE_NAME = "PACKAGE_NAME";
    private static final String CLASS_NAME = "CLASS_NAME";
    private static final String ADAPTER_TYPE = "ADAPTER_TYPE";
    private static final String DESTINATION_TYPE = "destinationType";
    private static final String DESTINATION_LOOKUP = "destinationLookup";
    private static final String MESSAGE_SELECTOR = "messageSelector";
    private static final String SUBSCRIPTION_DURABILITY = "subscriptionDurability";
    private static final String DURABLE = "Durable";
    private static final String CLIENT_ID = "clientId";
    private static final String SUBSCRIPTION_NAME = "subscriptionName";

    private final RamlValidator validator = new CompositeRamlValidator(
            new ResourceUriRamlValidator(),
            new ContainsResourcesRamlValidator(),
            new ContainsActionsRamlValidator(),
            new RequestContentTypeRamlValidator(),
            new BaseUriRamlValidator()
    );

    /**
     * Generates JMS endpoint classes out of RAML object
     *
     * @param raml          - the RAML object
     * @param configuration - contains package of generated sources, as well as source and
     *                      destination folders
     */
    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {

        validator.validate(raml);

        final Collection<Resource> ramlResourceModels = raml.getResources().values();
        final BaseUri baseUri = new BaseUri(raml.getBaseUri());
        ramlResourceModels.stream()
                .map(resource -> templateAttributesFrom(resource, baseUri, configuration))
                .forEach(attribute -> writeToTemplateFile(attribute, baseUri, jmsListenerTemplate(),
                        packageOutputPathFrom(configuration)));

    }

    private Path packageOutputPathFrom(final GeneratorConfig configuration) {
        return configuration.getOutputDirectory()
                .resolve(configuration.getBasePackageName().replace(".", File.separator));
    }

    @SuppressWarnings("resource")
    private String jmsListenerTemplate() {
        try (final InputStream stream = getClass().getResourceAsStream(JMS_TEMPLATE_RESOURCE)) {
            return new Scanner(stream, UTF_8).useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new JmsEndpointGeneratorException(TEMPLATE_LOADING_ERROR, e);
        }
    }

    private void writeToTemplateFile(final TemplateAttributes templateAttributes, final BaseUri baseUri, final String jmsTemplate, final Path outputPath) {
        final Path filePath = outputPath.resolve(createJmsFilenameFrom(templateAttributes.resourceUri, baseUri));
        try {
            write(filePath.toFile(), render(jmsTemplate, templateAttributes));
        } catch (IOException e) {
            throw new JmsEndpointGeneratorException(format(OUTPUT_FILE_GENERATION_ERROR, templateAttributes.resourceUri), e);
        }
    }

    private String createJmsFilenameFrom(final ResourceUri resourceUri, final BaseUri baseUri) {
        return classNameOf(resourceUri, componentOf(resourceUri, baseUri)) + FILENAME_POSTFIX;
    }

    /**
     * Create Template TemplateAttributes from the RAML Resource and Configuration
     *
     * @param resource      RAML resource
     * @param baseUri       RAML baseUri
     * @param configuration Configuration information  @return template attributes
     */
    private TemplateAttributes templateAttributesFrom(final Resource resource, final BaseUri baseUri, final GeneratorConfig configuration) {
        final ResourceUri resourceUri = new ResourceUri(resource.getUri());
        final List<Pair<String, String>> mainAttributes = new LinkedList<>();
        final List<Pair<String, String>> activationConfigAttributes = new LinkedList<>();
        final Component component = componentOf(resourceUri, baseUri);

        mainAttributes.add(Pair.of(PACKAGE_NAME, configuration.getBasePackageName()));
        mainAttributes.add(Pair.of(CLASS_NAME, classNameOf(resourceUri, component)));
        mainAttributes.add(Pair.of(ADAPTER_TYPE, component.name()));
        activationConfigAttributes.add(Pair.of(DESTINATION_TYPE, component.destinationType().getName()));
        activationConfigAttributes.add(Pair.of(DESTINATION_LOOKUP, resourceUri.destinationName()));
        activationConfigAttributes.add(Pair.of(MESSAGE_SELECTOR, messageSelectorsFrom(resource.getActions())));
        if (Topic.class.equals(component.destinationType())) {
            activationConfigAttributes.add(Pair.of(SUBSCRIPTION_DURABILITY, DURABLE));
            String clientId = baseUri.adapterClientId();
            activationConfigAttributes.add(Pair.of(CLIENT_ID, clientId));
            activationConfigAttributes.add(Pair.of(SUBSCRIPTION_NAME, subscriptionNameOf(resourceUri, clientId)));
        }

        return new TemplateAttributes(mainAttributes, activationConfigAttributes, resourceUri);
    }

    private String subscriptionNameOf(final ResourceUri resourceUri, final String clientId) {
        return format("%s.%s", clientId, resourceUri.destinationName());
    }

    /**
     * Convert given URI and component to a camel cased class name
     *
     * @param resourceUri URI String to convert
     * @return camel case class name
     */
    private String classNameOf(final ResourceUri resourceUri, final Component component) {
        return format("%s%s%s",
                capitalize(resourceUri.context()),
                capitalize(component.pillar()),
                capitalize(component.tier()));
    }

    /**
     * Convert given URI to a valid Component <p> Takes the last and second to last parts of the URI
     * as the pillar and tier of the Component
     *
     * @param resourceUri URI of the resource
     * @param baseUri     base uri of the resource
     * @return component the value of the pillar and tier parts of the uri
     */
    private Component componentOf(final ResourceUri resourceUri, final BaseUri baseUri) {

        final String pillar = resourceUri.pillar();
        final String tier = resourceUri.tier() != null ? resourceUri.tier() : baseUri.tier();
        return Component.valueOf(pillar, tier);
    }


    /**
     * Parse and format all the message selectors from the Post Action
     *
     * @param actions Map of ActionType to Action
     * @return formatted message selector String
     */
    private String messageSelectorsFrom(final Map<ActionType, Action> actions) {
        return format("CPPNAME in('%s')", parse(actions.get(ActionType.POST)));
    }

    /**
     * Parse an Action into a message selectors String
     *
     * @param action Action to parse
     * @return formatted message selectors String
     */
    private String parse(final Action action) {
        return action.getBody().keySet().stream()
                .map(this::commandNameOf)
                .collect(joining("','"));
    }

    /**
     * Converts media type String to a command name <p> Command name is equal to everything between
     * "application/vnd." and the first "+".
     *
     * @param mediaType String representation of the Media Type
     * @return command name
     */
    private String commandNameOf(final String mediaType) {
        final Matcher m = MEDIA_TYPE_PATTERN.matcher(mediaType);
        m.find();
        return m.group(2);
    }

    /**
     * @param template           - string containing template
     * @param templateAttributes - attributes to be used in rendering of the template
     * @return - rendered template
     */
    private String render(final String template, final TemplateAttributes templateAttributes) {
        ST st = new ST(template);
        templateAttributes.mainAttributes.forEach(a -> st.add(a.getKey(), a.getValue()));
        templateAttributes.activationConfigAttributes
                .forEach(a -> st.addAggr("ACTIVATION_CONFIG_PROPERTY.{NAME, VALUE}", a.getKey(), a.getValue()));
        return st.render();
    }

}
