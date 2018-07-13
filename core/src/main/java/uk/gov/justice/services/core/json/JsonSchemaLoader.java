package uk.gov.justice.services.core.json;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.schema.catalog.SchemaCatalogResolver;

import java.io.InputStream;

import javax.inject.Inject;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;

/**
 * Service for loading JSON schemas.
 */
public class JsonSchemaLoader {

    private Logger logger = getLogger(JsonSchemaLoader.class);

    private final static String SCHEMA_LOCATION_PATTERN = "/json/schema/%s.json";

    @Inject
    SchemaCatalogResolver schemaCatalogResolver;

    /**
     * Locate a JSON schema file on the classpath and load it.
     *
     * @param actionName the logical name for the JSON type
     * @return the schema
     */
    public Schema loadSchema(final String actionName) {
        final String schemaFile = format(SCHEMA_LOCATION_PATTERN, actionName);
        return load(schemaFile);
    }

    private Schema load(final String schemaFile) {
        logger.trace("Loading schema {}", schemaFile);

        try (final InputStream schemaFileStream = this.getClass().getResourceAsStream(schemaFile)) {
            final JSONObject schemaJsonObject = new JSONObject(new JSONTokener(schemaFileStream));
            return schemaCatalogResolver.loadSchema(schemaJsonObject);
        } catch (final Exception ex) {
            throw new SchemaLoadingException(format("Unable to load JSON schema %s from classpath", schemaFile), ex);
        }
    }
}
