package uk.gov.justice.raml.jms.core;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.write;
import static uk.gov.justice.raml.jms.core.TemplateMarker.render;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.core.annotation.Component;

public class JmsEndpointGenerator implements Generator {

    private static final String FILENAME_POSTFIX = "JmsListener.java";
    private static final String JMS_TEMPLATE_RESOURCE = "JmsListenerTemplate.tpm";
    private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("(application/vnd.)(\\S+)(\\+\\S+)");

    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {
        final File outputDirectory = createOutputDirectories(configuration);
        final String jmsTemplate = jmsListenerTemplate();
        final Collection<Resource> ramlResourceModels = raml.getResources().values();

        ramlResourceModels.stream()
                .map(resource -> templateModelFrom(resource, configuration))
                .forEach(model -> writeToTemplateFile(model, jmsTemplate, outputDirectory));

    }

    private void writeToTemplateFile(final TemplateModel templateModel, final String jmsTemplate, final File outputDirectory) {
        final File file = new File(outputDirectory, createJmsFilenameFrom(templateModel.uri));
        try {
            write(file, render(jmsTemplate, templateModel.model));
        } catch (IOException e) {
            throw new JmsEndpointGeneratorException(format("Failed to create output file for %s", templateModel.uri), e);
        }
    }

    private TemplateModel templateModelFrom(final Resource resource, final GeneratorConfig configuration) {
        final String uri = resource.getUri();
        final HashMap<String, String> data = new HashMap<>();

        data.put("PACKAGE_NAME", configuration.getBasePackageName());
        data.put("CLASS_NAME", classNameOf(uri));
        data.put("ADAPTER_TYPE", componentOf(uri).name());
        data.put("DESTINATION_LOOKUP", destinationNameOf(uri));
        data.put("MESSAGE_SELECTOR", messageSelectorsFrom(resource.getActions()));

        return new TemplateModel(data, uri);
    }

    private String messageSelectorsFrom(final Map<ActionType, Action> actions) {
        if (actions.isEmpty()) {
            throw new JmsEndpointGeneratorException("No Actions to parse to Message Selectors");
        }
        return "'" + parse(actions.get(ActionType.POST)) + "'";
    }

    private String parse(final Action action) {
        return action.getBody().keySet().stream().map(this::commandNameOf).collect(Collectors.joining("','"));
    }

    private String commandNameOf(final String mediaType) {
        final Matcher m = MEDIA_TYPE_PATTERN.matcher(mediaType);
        m.find();
        return m.group(2);
    }

    private String destinationNameOf(final String uri) {
        return uri.replaceAll("/", "");
    }

    private Component componentOf(final String uri) {
        final String[] uriParts = uri.split("\\.");
        return Component.valueOf(uriParts[uriParts.length - 1], uriParts[uriParts.length - 2]);
    }

    @SuppressWarnings("resource")
    private String jmsListenerTemplate() {
        try (final InputStream stream = getClass().getResourceAsStream(JMS_TEMPLATE_RESOURCE)) {
            return new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new JmsEndpointGeneratorException("Failed to load template resource JmsListenerTemplate.tpm", e);
        }
    }

    private String createJmsFilenameFrom(final String uri) {
        return classNameOf(uri) + FILENAME_POSTFIX;
    }

    private String classNameOf(final String uri) {
        return toCamelCase(uri);
    }

    private String toCamelCase(final String s) {
        final String[] parts = s.split("/|\\.");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    private String toProperCase(final String s) {
        if (!s.isEmpty()) {
            return s.substring(0, 1).toUpperCase() +
                    s.substring(1).toLowerCase();
        }
        return s;
    }

    private File createOutputDirectories(final GeneratorConfig configuration) {
        final File packageFolder = new File(format("%s/%s", configuration.getOutputDirectory(),
                configuration.getBasePackageName().replace(".", "/")));
        packageFolder.mkdirs();
        return packageFolder;
    }

    private class TemplateModel {
        final Map<String, String> model;
        final String uri;

        public TemplateModel(final Map<String, String> model, final String uri) {
            this.model = model;
            this.uri = uri;
        }
    }

}
