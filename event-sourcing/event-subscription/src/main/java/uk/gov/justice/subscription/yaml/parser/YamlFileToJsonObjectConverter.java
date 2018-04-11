package uk.gov.justice.subscription.yaml.parser;

import static java.lang.String.format;

import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

public class YamlFileToJsonObjectConverter {

    private final YamlParser yamlParser;
    private final ObjectMapper objectMapper;

    public YamlFileToJsonObjectConverter(final YamlParser yamlParser, final ObjectMapper objectMapper) {
        this.yamlParser = yamlParser;
        this.objectMapper = objectMapper;
    }

    public JSONObject convert(final Path filePath) {
        final Object yamlObject = yamlParser.parseYamlFrom(filePath, Object.class);

        try {
            return new JSONObject(objectMapper.writeValueAsString(yamlObject));
        } catch (final JsonProcessingException e) {
            throw new YamlToJsonObjectException(format("Failed to convert YAML to JSON for %s", filePath.toAbsolutePath()), e);
        }
    }
}
