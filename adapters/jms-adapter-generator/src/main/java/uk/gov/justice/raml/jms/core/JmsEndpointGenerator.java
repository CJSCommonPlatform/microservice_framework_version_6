package uk.gov.justice.raml.jms.core;

import static org.apache.commons.io.FileUtils.write;
import static uk.gov.justice.raml.jms.core.TemplateMarker.render;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.raml.model.Raml;
import org.raml.model.Resource;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.core.annotation.Component;

public class JmsEndpointGenerator implements Generator {

    private static final String FILENAME_POSTFIX = "JmsListener.java";

    @Override
    public void run(Raml raml, GeneratorConfig configuration) {
        final File outputDirectory = createOutputDirectories(configuration);
        final String jmsTemplate = jmsListenerTemplate();

        final Collection<Resource> ramlResourceModels = raml.getResources().values();

        for (Resource ramlResourceModel : ramlResourceModels) {

            String uri = ramlResourceModel.getUri();
            File file = new File(outputDirectory, createJmsFilenameFrom(uri));

            try {
                final HashMap<String, String> templateData = new HashMap<>();
                templateData.put("PACKAGE_NAME", configuration.getBasePackageName());
                templateData.put("CLASS_NAME", classNameOf(uri));
                templateData.put("ADAPTER_TYPE", componentOf(uri).name());
                templateData.put("DESTINATION_LOOKUP", destinationNameOf(uri));

                write(file, render(jmsTemplate, templateData));
            } catch (IOException e) {
                throw new JmsEndpointGeneratorException(String.format("Failed to create output file for %s", uri), e);
            }
        }
    }

    private String destinationNameOf(String uri) {
        return uri.replaceAll("/", "");
    }

    private Component componentOf(String uri) {
        String[] uriParts = uri.split("\\.");
        Component component = Component.valueOf(uriParts[uriParts.length-1], uriParts[uriParts.length-2]);
        return component;
    }

    @SuppressWarnings("resource")
    private String jmsListenerTemplate() {
        try (final InputStream stream = getClass().getResourceAsStream("JmsListenerTemplate.tpm")) {
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

    private String toCamelCase(String s) {
        String[] parts = s.split("/|\\.");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    private String toProperCase(String s) {
        if (!s.isEmpty()) {
            return s.substring(0, 1).toUpperCase() +
                    s.substring(1).toLowerCase();
        }
        return s;
    }

    private File createOutputDirectories(GeneratorConfig configuration) {
        File packageFolder = new File(String.format("%s/%s", configuration.getOutputDirectory(),
                configuration.getBasePackageName().replace(".", "/")));
        packageFolder.mkdirs();
        return packageFolder;
    }

}
