package uk.gov.justice.services.core.json;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
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
    FileSystemUrlResolverStrategy fileSystemUrlResolverStrategy;

    /**
     * Locate a JSON schema file on the classpath and load it.
     *
     * @param name the logical name for the JSON type
     * @return the schema
     */
    public Schema loadSchema(final String name) {
        final String schemaFile = format(SCHEMA_LOCATION_PATTERN, name);
        return load(schemaFile);
    }

    private Schema load(final String schemaFile) {
        logger.trace("Loading schema {}", schemaFile);
        try (final InputStream schemaFileStream = this.getClass().getResourceAsStream(schemaFile)){
            final URL schemaUrl = getClass().getResource(schemaFile);
            return SchemaLoader.builder()
                    .resolutionScope(resolveUrl(schemaUrl))
                    .schemaJson(
                            new JSONObject(new JSONTokener(schemaFileStream)))
                    .build().load().build();
        } catch (final Exception ex) {
            throw new SchemaLoadingException(format("Unable to load JSON schema %s from classpath", schemaFile), ex);
        }
    }

    private String resolveUrl(final URL schemaUrl) throws URISyntaxException, IOException {
        return fileSystemUrlResolverStrategy.getPhysicalFrom(schemaUrl).toString();
    }
}
