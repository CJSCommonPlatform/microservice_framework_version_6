package uk.gov.justice.services.generators.commons.mapping;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.VALID_JSON_SCHEMA;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.MimeType;

public class SchemaIdParserTest {

    private static final String SCHEMA_ID = "http://justice.gov.uk/example/command/api/command1.json";
    private static final String MEDIA_TYPE = "application/vnd.ctx.command.command1+json";

    private static final String NO_ID_JSON_SCHEMA = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"userUrn\": {\n" +
            "      \"id\": \"/urn\",\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"userUrn\"\n" +
            "  ]\n" +
            "}";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private final SchemaIdParser schemaIdParser = new SchemaIdParser();

    @Test
    public void shouldParseSchemaIdFromJsonSchemaFromMediaType() throws Exception {
        final MimeType mimeType = new MimeType();
        mimeType.setType(MEDIA_TYPE);
        mimeType.setSchema(format(VALID_JSON_SCHEMA, SCHEMA_ID));

        final Optional<String> schemaId = schemaIdParser.schemaIdFrom(mimeType);

        assertThat(schemaId, is(Optional.of(SCHEMA_ID)));
    }

    @Test
    public void shouldThrowExceptionIfJsonSchemaHasBlankSchemaId() throws Exception {
        final MimeType mimeType = new MimeType();
        mimeType.setType(MEDIA_TYPE);
        mimeType.setSchema(format(VALID_JSON_SCHEMA, ""));

        expectedException.expect(SchemaParsingException.class);
        expectedException.expectMessage(is("Schema for media type: " + MEDIA_TYPE + " has a blank schema id"));

        schemaIdParser.schemaIdFrom(mimeType);
    }

    @Test
    public void shouldReturnEmptyIfJsonSchemaHasNoSchemaId() throws Exception {
        final MimeType mimeType = new MimeType();
        mimeType.setType(MEDIA_TYPE);
        mimeType.setSchema(NO_ID_JSON_SCHEMA);

        assertThat(schemaIdParser.schemaIdFrom(mimeType), is(empty()));
    }

    @Test
    public void shouldReturnOptionalEmptyIfJsonSchemaIsNotSet() throws Exception {
        final MimeType mimeType = new MimeType();
        mimeType.setType(MEDIA_TYPE);

        schemaIdParser.schemaIdFrom(mimeType);
    }
}
