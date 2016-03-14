package uk.gov.justice.raml.jms.core;

import org.apache.commons.lang.StringUtils;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.core.annotation.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.io.FileUtils.write;
import static uk.gov.justice.raml.jms.core.TemplateRenderer.render;

/**
 * Generates JMS endpoint classes out of RAML object
 */
public class JmsEndpointGenerator implements Generator {

    private static final String UTF_8 = "UTF-8";
    private static final String TEMPLATE_LOADING_ERROR = "Failed to load template resource JmsListenerTemplate.tpm";
    private static final String ACTIONS_EMPTY_ERROR = "No actions to process";
    private static final String OUTPUT_FILE_GENERATION_ERROR = "Failed to create output file for %s";
    private static final String FILENAME_POSTFIX = "JmsListener.java";
    private static final String JMS_TEMPLATE_RESOURCE = "JmsListenerTemplate.tpm";
    private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("(application/vnd.)(\\S+)(\\+\\S+)");
    private static final String EMPTY = "";

    /**
     * Generates JMS endpoint classes out of RAML object
     *
     * @param raml          - the RAML object
     * @param configuration - contains package of generated sources, as well as source and
     *                      destination folders
     */
    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {
        final Collection<Resource> ramlResourceModels = raml.getResources().values();
        ramlResourceModels.stream()
                .map(resource -> templateAttributesFrom(resource, configuration))
                .forEach(attribute -> writeToTemplateFile(attribute, jmsListenerTemplate(), outputDirFrom(configuration)));

    }

    private File outputDirFrom(final GeneratorConfig configuration) {
        return new File(format("%s/%s", configuration.getOutputDirectory(),
                configuration.getBasePackageName().replace(".", "/")));
    }

    @SuppressWarnings("resource")
    private String jmsListenerTemplate() {
        try (final InputStream stream = getClass().getResourceAsStream(JMS_TEMPLATE_RESOURCE)) {
            return new Scanner(stream, UTF_8).useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new JmsEndpointGeneratorException(TEMPLATE_LOADING_ERROR, e);
        }
    }

    private void writeToTemplateFile(final Attributes attributes, final String jmsTemplate,
                                     final File outputDirectory) {
        final File file = new File(outputDirectory, createJmsFilenameFrom(attributes.uri));
        try {
            write(file, render(jmsTemplate, attributes.attributesMap));
        } catch (IOException e) {
            throw new JmsEndpointGeneratorException(format(OUTPUT_FILE_GENERATION_ERROR, attributes.uri), e);
        }
    }

    private String createJmsFilenameFrom(final String uri) {
        return classNameOf(uri) + FILENAME_POSTFIX;
    }

    /**
     * Create Template Attributes from the RAML Resource and Configuration
     *
     * @param resource RAML Resource
     * @param configuration Configuration information
     * @return template attributes
     */
    private Attributes templateAttributesFrom(final Resource resource, final GeneratorConfig configuration) {
        final String uri = resource.getUri();
        final HashMap<String, String> data = new HashMap<>();

        data.put("PACKAGE_NAME", configuration.getBasePackageName());
        data.put("CLASS_NAME", classNameOf(uri));
        data.put("ADAPTER_TYPE", componentOf(uri).name());
        data.put("DESTINATION_LOOKUP", destinationNameOf(uri));
        data.put("MESSAGE_SELECTOR", messageSelectorsFrom(resource.getActions()));

        return new Attributes(data, uri);
    }

    /**
     * Convert given URI to a camel cased class name
     *
     * @param uri URI String to convert
     * @return camel case class name
     */
    private String classNameOf(final String uri) {
        return stream(uri.split("/|\\."))
                .map(StringUtils::capitalize)
                .collect(joining(EMPTY));
    }

    /**
     * Convert given URI to a valid Component
     *
     * Takes the last and second to last parts of the URI as the pillar and tier of the Component
     *
     * @param uri URI String to convert
     * @return component the value of the pillar and tier parts of the uri
     */
    private Component componentOf(final String uri) {
        final String[] uriParts = uri.split("\\.");
        return Component.valueOf(uriParts[uriParts.length - 1], uriParts[uriParts.length - 2]);
    }

    /**
     * Construct the destination name from the URI
     * @param uri URI String to convert
     * @return destination name
     */
    private String destinationNameOf(final String uri) {
        return uri.replaceAll("/", "");
    }

    /**
     * Parse and format all the message selectors from the Post Action
     *
     * @param actions Map of ActionType to Action
     * @return formatted message selector String
     */
    private String messageSelectorsFrom(final Map<ActionType, Action> actions) {
        if (actions.isEmpty()) {
            throw new JmsEndpointGeneratorException(ACTIONS_EMPTY_ERROR);
        }
        return format("'%s'", parse(actions.get(ActionType.POST)));
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
     * Converts media type String to a command name
     *
     * Command name is equal to everything between "application/vnd." and the first "+".
     *
     * @param mediaType String representation of the Media Type
     * @return command name
     */
    private String commandNameOf(final String mediaType) {
        final Matcher m = MEDIA_TYPE_PATTERN.matcher(mediaType);
        m.find();
        return m.group(2);
    }

    private class Attributes {
        final Map<String, String> attributesMap;
        final String uri;

        public Attributes(final Map<String, String> attributesMap, final String uri) {
            this.attributesMap = attributesMap;
            this.uri = uri;
        }
    }

}
