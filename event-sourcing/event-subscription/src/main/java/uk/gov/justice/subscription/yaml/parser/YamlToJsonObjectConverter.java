package uk.gov.justice.subscription.yaml.parser;

import static java.lang.String.format;

import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

public class YamlToJsonObjectConverter {

    private final YamlParser yamlParser;
    private final ObjectMapper objectMapper;

    public YamlToJsonObjectConverter(final YamlParser yamlParser, final ObjectMapper objectMapper) {
        this.yamlParser = yamlParser;
        this.objectMapper = objectMapper;
    }

    public JSONObject convert(final URL url) {
        final Object yamlObject = yamlParser.parseYamlFrom(url, Object.class);

        try {
            return new JSONObject(objectMapper.writeValueAsString(yamlObject));
        } catch (final JsonProcessingException e) {
            throw new YamlToJsonObjectException(format("Failed to convert YAML to JSON for %s", url), e);
        }
    }
}
