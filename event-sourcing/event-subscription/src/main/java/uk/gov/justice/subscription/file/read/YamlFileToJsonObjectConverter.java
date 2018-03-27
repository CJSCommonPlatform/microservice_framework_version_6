package uk.gov.justice.subscription.file.read;

import static java.nio.file.Files.readAllLines;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.json.JSONObject;

public class YamlFileToJsonObjectConverter {

    final String lineSeparator = System.getProperty("line.separator");

    public JSONObject convert(final Path filePath) throws IOException {

        final String yamlStr = readAllLines(filePath)
                .stream()
                .collect(joining(lineSeparator));

        final Object yamlObject = new ObjectMapper(new YAMLFactory())
                .readValue(yamlStr, Object.class);

        final ObjectMapper jsonWriter = new ObjectMapper();

        return new JSONObject(jsonWriter.writeValueAsString(yamlObject));
    }
}
