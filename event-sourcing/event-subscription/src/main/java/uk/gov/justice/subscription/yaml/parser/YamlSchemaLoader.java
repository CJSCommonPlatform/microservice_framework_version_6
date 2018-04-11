package uk.gov.justice.subscription.yaml.parser;

import java.io.IOException;
import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class YamlSchemaLoader {

    public Schema loadSchema(final String schemaFileLocation) throws IOException {
        try (final InputStream schemaFileStream = this.getClass().getResourceAsStream(schemaFileLocation)) {
            return SchemaLoader.builder()
                    .schemaJson(new JSONObject(new JSONTokener(schemaFileStream)))
                    .build().load().build();
        }
    }
}
