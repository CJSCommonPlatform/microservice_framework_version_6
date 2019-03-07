package uk.gov.justice.subscription.yaml.parser;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URL;

import org.json.JSONObject;

public class YamlFileValidator {

    private static final String EVENT_SOURCES_SCHEMA_PATH = "/json/schema/event-source-schema.json";
    private static final String SUBSCRIPTION_SCHEMA_PATH = "/json/schema/subscription-schema.json";

    private final YamlToJsonObjectConverter yamlToJsonObjectConverter;
    private final YamlSchemaLoader yamlSchemaLoader;

    public YamlFileValidator(final YamlToJsonObjectConverter yamlToJsonObjectConverter, final YamlSchemaLoader yamlSchemaLoader) {
        this.yamlToJsonObjectConverter = yamlToJsonObjectConverter;
        this.yamlSchemaLoader = yamlSchemaLoader;
    }

    public void validateEventSource(final URL yamlUrl) {
        validate(yamlUrl, EVENT_SOURCES_SCHEMA_PATH);
    }

    public void validateSubscription(final URL yamlUrl) {
        validate(yamlUrl, SUBSCRIPTION_SCHEMA_PATH);
    }

    private void validate(final URL yamlUrl, final String schemaFileLocation) {

        final JSONObject yamlAsJson = yamlToJsonObjectConverter.convert(yamlUrl);
        try {
            yamlSchemaLoader.loadSchema(schemaFileLocation).validate(yamlAsJson);
        } catch (final IOException ex) {
            throw new YamlParserException(format("Unable to load JSON schema %s from classpath", schemaFileLocation), ex);
        }
    }
}
