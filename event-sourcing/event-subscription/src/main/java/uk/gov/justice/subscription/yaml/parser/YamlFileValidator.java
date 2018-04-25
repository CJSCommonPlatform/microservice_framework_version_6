package uk.gov.justice.subscription.yaml.parser;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.file.Path;

import org.json.JSONObject;

public class YamlFileValidator {

    private static final String EVENT_SOURCES_SCHEMA_PATH = "/json/schema/event-source-schema.json";
    private static final String SUBSCRIPTION_SCHEMA_PATH = "/json/schema/subscription-schema.json";

    private final YamlFileToJsonObjectConverter yamlFileToJsonObjectConverter;
    private final YamlSchemaLoader yamlSchemaLoader;

    public YamlFileValidator(final YamlFileToJsonObjectConverter yamlFileToJsonObjectConverter, final YamlSchemaLoader yamlSchemaLoader) {
        this.yamlFileToJsonObjectConverter = yamlFileToJsonObjectConverter;
        this.yamlSchemaLoader = yamlSchemaLoader;
    }

    public void validateEventSource(final Path yamlFilePath) {
        validate(yamlFilePath, EVENT_SOURCES_SCHEMA_PATH);
    }

    public void validateSubscription(final Path yamlFilePath) {
        validate(yamlFilePath, SUBSCRIPTION_SCHEMA_PATH);
    }

    private void validate(final Path yamlFilePath, final String schemaFileLocation) {

        final JSONObject yamlAsJson = yamlFileToJsonObjectConverter.convert(yamlFilePath);
        try {
            yamlSchemaLoader.loadSchema(schemaFileLocation).validate(yamlAsJson);
        } catch (final IOException ex) {
            throw new YamlParserException(format("Unable to load JSON schema %s from classpath", schemaFileLocation), ex);
        }
    }
}
