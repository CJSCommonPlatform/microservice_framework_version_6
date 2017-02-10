package uk.gov.justice.services.core.json;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.everit.json.schema.loader.SchemaLoader.load;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for loading JSON schemas.
 */
public class JsonSchemaLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaLoader.class);
    private final static String SCHEMA_LOCATION_PATTERN = "/json/schema/%s.json";

    /**
     * Locate a JSON schema file on the classpath and load it.
     * @param name the logical name for the JSON type
     * @return the schema
     */
    public Schema loadSchema(final String name) {
        final String schemaFile = format(SCHEMA_LOCATION_PATTERN, name);
        return load(streamOf(schemaFile), schemaFile);
    }

    private Schema load(final InputStream fileStream, final String schemaFile) {
        try {
            return SchemaLoader.load(new JSONObject(IOUtils.toString(fileStream, defaultCharset().name())));
        } catch (final Exception ex) {
            throw new SchemaLoadingException(format("Unable to load JSON schema %s from classpath", schemaFile),  ex);
        }
    }

    private InputStream streamOf(final String schemaFile) {
        LOGGER.trace("Loading schema {}", schemaFile);
        final InputStream inputStream = this.getClass().getResourceAsStream(schemaFile);

        if (inputStream == null) {
            throw new SchemaLoadingException(format("JSON schema %s not found on classpath", schemaFile));
        }
        return inputStream;
    }
}
