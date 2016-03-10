package uk.gov.justice.raml.jms.core;

import org.apache.commons.io.FileUtils;
import org.raml.model.Raml;
import org.raml.model.Resource;
import uk.gov.justice.raml.core.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class JmsEndpointGenerator implements Generator {

    private static final String FILENAME_POSTFIX = "JmsListener.java";

    @Override
    public Set<String> run(Raml raml, Configuration configuration) {
        final File outputDirectory = createOutputDirectories(configuration);
        final Set<String> generatedFiles = new HashSet<>();
        final String jmsTemplate = jmsListenerTemplate();

        final Collection<Resource> ramlResourceModels = raml.getResources().values();

        for (final Resource ramlResourceModel : ramlResourceModels) {

            final String uri = ramlResourceModel.getUri();

            final File file = new File(outputDirectory, createJmsFilenameFrom(uri));
            generatedFiles.add(file.getName());

            try {
                final HashMap<String, String> templateData = new HashMap<>();
                templateData.put("PACKAGE_NAME", configuration.getBasePackageName());
                templateData.put("CLASS_NAME", createJmsListenerClassNameFrom(uri));

                FileUtils.write(file, TemplateMarker.mark(jmsTemplate, templateData));
            } catch (IOException e) {
                throw new JmsEndpointGeneratorException(String.format("Failed to create output file for %s", uri), e);
            }

        }

        return generatedFiles;
    }

    private String jmsListenerTemplate() {
        try (final InputStream stream = getClass().getResourceAsStream("JmsListenerTemplate.tpm")) {
            return new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new JmsEndpointGeneratorException("Failed to load template resource JmsListenerTemplate.tpm", e);
        }
    }

    private String createJmsFilenameFrom(final String uri) {
        return createJmsListenerClassNameFrom(uri) + FILENAME_POSTFIX;
    }

    private String createJmsListenerClassNameFrom(final String uri) {
        return toCamelCase(uri);
    }

    private String toCamelCase(String s) {
        String[] parts = s.split("/");
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

    private File createOutputDirectories(Configuration configuration) {
        File outputDirectory = configuration.getOutputDirectory();
        File packageFolder = new File(String.format("%s/%s", outputDirectory.getAbsolutePath(), configuration.getBasePackageName().replace(".", "/")));
        packageFolder.mkdirs();
        return packageFolder;
    }

}
