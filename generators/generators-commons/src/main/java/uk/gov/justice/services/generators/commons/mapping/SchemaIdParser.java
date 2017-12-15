package uk.gov.justice.services.generators.commons.mapping;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import uk.gov.justice.services.messaging.JsonObjects;

import java.io.StringReader;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.raml.model.MimeType;

/**
 * Parses a schema id from the RAML {@link MimeType} object. Will return empty() if no schema
 * is found.
 */
public class SchemaIdParser {

    /**
     * Get the schema id from the schemaJson of the given MimeType.
     *
     * @param mimeType the {@link MimeType} to parse
     * @return schema id String or empty if not found
     */
    public Optional<String> schemaIdFrom(final MimeType mimeType) {
        return ofNullable(mimeType.getSchema()).flatMap(s -> schemaIdFrom(s, mimeType));
    }

    private Optional<String> schemaIdFrom(final String schemaJson, final MimeType mimeType) {
        try (final JsonReader reader = Json.createReader(new StringReader(schemaJson))) {
            final JsonObject jsonObject = reader.readObject();
            final Optional<String> schemaId = JsonObjects.getString(jsonObject, "id");

            schemaId.ifPresent(id -> {
                if (id.isEmpty())
                    throw new SchemaParsingException(format("Schema for media type: %s has a blank schema id", mimeType.getType()));
            });

            return schemaId;
        }
    }
}
