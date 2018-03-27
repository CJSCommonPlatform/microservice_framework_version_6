package uk.gov.justice.subscription.file.read;

import static java.lang.String.format;

import uk.gov.justice.subscription.SubscriptionDescriptorException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SubscriptionDescriptorFileValidator {

    private YamlFileToJsonObjectConverter yamlFileToJsonObjectConverter;

    private static final String SCHEMA_FILE = "/json/schema/subscription-schema.json";

    public SubscriptionDescriptorFileValidator(final YamlFileToJsonObjectConverter yamlFileToJsonObjectConverter) {
        this.yamlFileToJsonObjectConverter = yamlFileToJsonObjectConverter;
    }

    public void validate(final Path filePath) {
        try {
            final JSONObject convert = yamlFileToJsonObjectConverter.convert(filePath);
            schema().validate(convert);
        } catch (IOException ex) {
            throw new SubscriptionDescriptorException(format("Unable to convert to JSON file %s ", filePath.toString()), ex);
        }

    }

    private Schema schema() {
        try (final InputStream schemaFileStream = this.getClass().getResourceAsStream(SCHEMA_FILE)) {
            return SchemaLoader.builder()
                    .schemaJson(new JSONObject(new JSONTokener(schemaFileStream)))
                    .build().load().build();
        } catch (final IOException ex) {
            throw new SubscriptionDescriptorException(format("Unable to load JSON schema %s from classpath", SCHEMA_FILE), ex);
        }
    }
}
