package uk.gov.justice.services.core.json;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCache;

import java.util.Optional;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SchemaCatalogAwareJsonSchemaValidatorTest {

    @Mock
    private Logger logger;

    @Mock
    private FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator;

    @Mock
    private SchemaIdMappingCache schemaIdMappingCache;

    @Mock
    private SchemaCatalogService schemaCatalogService;

    @Mock
    private PayloadExtractor payloadExtractor;

    @InjectMocks
    private SchemaCatalogAwareJsonSchemaValidator schemaCatalogAwareJsonSchemaValidator;

    @Test
    public void shouldLoadTheCorrectSchemaUsingTheCatalogServiceAndValidate() throws Exception {

        final String uri = "http://space.time.gov.uk/mind/command/api/initiate-warp-speed.json";
        final Optional<String> schemaId = of(uri);
        final MediaType mediaType = new MediaType("application", "vnd.mind.command.initiate-warp-speed+json");

        final String envelopeJson = "{\"envelope\": \"json\"}";

        final Schema schema = mock(Schema.class);
        final JSONObject payload = mock(JSONObject.class);

        when(schemaIdMappingCache.schemaIdFor(mediaType)).thenReturn(schemaId);
        when(schemaCatalogService.findSchema(uri)).thenReturn(of(schema));
        when(payloadExtractor.extractPayloadFrom(envelopeJson)).thenReturn(payload);

        schemaCatalogAwareJsonSchemaValidator.validate(envelopeJson, mediaType);

        verify(logger).info("Performing schema validation with catalog schema for: {}", mediaType);
        verify(schema).validate(payload);
        verifyZeroInteractions(fileBasedJsonSchemaValidator);
    }

    @Test
    public void shouldValidateUsingTheVanillaJsonSchemaValidatorIfNoSchemaMappedToTheSchemaId() throws Exception {

        final MediaType mediaType = new MediaType("application", "vnd.mind.command.initiate-warp-speed+json");
        final Optional<String> schemaId = empty();

        final String envelopeJson = "{\"envelope\": \"json\"}";

        when(schemaIdMappingCache.schemaIdFor(mediaType)).thenReturn(schemaId);

        schemaCatalogAwareJsonSchemaValidator.validate(envelopeJson, mediaType);

        verify(logger).info("Falling back to file base schema lookup, no catalog schema found for: {}", mediaType);
        verify(fileBasedJsonSchemaValidator).validate(envelopeJson, mediaType);
    }

    @Test
    public void shouldValidateUsingTheVanillaJsonSchemaValidatorIfTheCatalogServiceDoesNotHaveTheCorrectSchemaMapped() throws Exception {

        final String uri = "http://space.time.gov.uk/mind/command/api/initiate-warp-speed.json";
        final Optional<String> schemaId = of(uri);
        final MediaType mediaType = new MediaType("application", "vnd.mind.command.initiate-warp-speed+json");

        final String envelopeJson = "{\"envelope\": \"json\"}";

        when(schemaIdMappingCache.schemaIdFor(mediaType)).thenReturn(schemaId);
        when(schemaCatalogService.findSchema(uri)).thenReturn(empty());

        schemaCatalogAwareJsonSchemaValidator.validate(envelopeJson, mediaType);

        verify(logger).info("Falling back to file base schema lookup, no catalog schema found for: {}", mediaType);
        verify(fileBasedJsonSchemaValidator).validate(envelopeJson, mediaType);
    }
}
