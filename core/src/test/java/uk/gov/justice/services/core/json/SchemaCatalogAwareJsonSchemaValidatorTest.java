package uk.gov.justice.services.core.json;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCache;

import java.util.Optional;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
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
    Logger logger;

    @Mock
    FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator;

    @Mock
    SchemaIdMappingCache schemaIdMappingCache;

    @Mock
    SchemaCatalogService schemaCatalogService;

    @Mock
    PayloadExtractor payloadExtractor;

    @InjectMocks
    private SchemaCatalogAwareJsonSchemaValidator schemaCatalogAwareJsonSchemaValidator;

    @Test
    public void shouldLoadTheCorrectSchemaUsingTheCatalogServiceAndValidate() throws Exception {

        final String uri = "http://space.time.gov.uk/mind/command/api/initiate-warp-speed.json";
        final String actionName = "command.api.initiate-warp-speed";
        final Optional<String> schemaId = of(uri);
        final MediaType mediaType = new MediaType("application", "vnd.mind.command.initiate-warp-speed+json");

        final String envelopeJson = "{\"envelope\": \"json\"}";

        final Schema schema = mock(Schema.class);
        final JSONObject payload = mock(JSONObject.class);

        when(schemaIdMappingCache.schemaIdFor(mediaType)).thenReturn(schemaId);
        when(schemaCatalogService.findSchema(uri)).thenReturn(of(schema));
        when(payloadExtractor.extractPayloadFrom(envelopeJson)).thenReturn(payload);

        schemaCatalogAwareJsonSchemaValidator.validate(envelopeJson, actionName, of(mediaType));

        verify(schema).validate(payload);
        verifyZeroInteractions(fileBasedJsonSchemaValidator);
    }

    @Test
    public void shouldFallBackToFileBasedSchemaValidationIfNoSchemaFoundInTheCatalogCache() throws Exception {

        final String uri = "http://space.time.gov.uk/mind/command/api/initiate-warp-speed.json";
        final String actionName = "command.api.initiate-warp-speed";
        final Optional<String> schemaId = of(uri);
        final MediaType mediaType = new MediaType("application", "vnd.mind.command.initiate-warp-speed+json");

        final String envelopeJson = "{\"envelope\": \"json\"}";

        when(schemaIdMappingCache.schemaIdFor(mediaType)).thenReturn(schemaId);
        when(schemaCatalogService.findSchema(uri)).thenReturn(empty());

        schemaCatalogAwareJsonSchemaValidator.validate(envelopeJson, actionName, of(mediaType));

        verify(fileBasedJsonSchemaValidator).validateWithoutSchemaCatalog(envelopeJson, actionName);

        verifyZeroInteractions(logger);
        verifyZeroInteractions(payloadExtractor);
    }

    @Test
    public void shouldThrowExceptionIfSchemaValidationFails() {

        final String uri = "http://space.time.gov.uk/mind/command/api/initiate-warp-speed.json";
        final String actionName = "command.api.initiate-warp-speed";
        final Optional<String> schemaId = of(uri);
        final MediaType mediaType = new MediaType("application", "vnd.mind.command.initiate-warp-speed+json");

        final String envelopeJson = "{\"envelope\": \"json\"}";

        final Schema schema = mock(Schema.class);
        final JSONObject payload = mock(JSONObject.class);

        when(schemaIdMappingCache.schemaIdFor(mediaType)).thenReturn(schemaId);
        when(schemaCatalogService.findSchema(uri)).thenReturn(of(schema));
        when(payloadExtractor.extractPayloadFrom(envelopeJson)).thenReturn(payload);
        doThrow(new ValidationException(schema, "", "", "")).when(schema).validate(payload);

        try {
            schemaCatalogAwareJsonSchemaValidator.validate(envelopeJson, actionName, of(mediaType));
            fail();
        } catch (final JsonSchemaValidationException e) {
            assertThat(e.getCause(), is(instanceOf(ValidationException.class)));
        }
    }
}
