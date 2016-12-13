package uk.gov.justice.services.core.json;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * Service for loading JSON schemas.
 */
@ApplicationScoped
public class JsonSchemaLoader {

    private final static String SCHEMA_LOCATION_PATTERN = "/json/schema/%s.json";

    @Inject
    private Logger logger;


    /**
     * Locate a JSON schema file on the classpath and load it.
     * @param name the logical name for the JSON type
     * @return the schema
     */
    public Schema loadSchema(final String name) {
        final String schemaFile = format(SCHEMA_LOCATION_PATTERN, name);
        logger.trace("Loading schema {}", schemaFile);
        final InputStream inputStream = this.getClass().getResourceAsStream(schemaFile);

        if (inputStream == null) {
            throw new IllegalStateException(format("JSON schema %s not found on classpath", schemaFile));
        }

        try {
            final JSONObject schemaJsonObject = new JSONObject(IOUtils.toString(inputStream, defaultCharset().name()));
            return SchemaLoader.load(schemaJsonObject);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load JSON schema from classpath", ex);
        }
    }
}
