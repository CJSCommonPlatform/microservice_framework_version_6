package uk.gov.justice.services.core.json;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link FileBasedJsonSchemaValidator} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileBasedJsonSchemaValidatorTest {

    @Mock
    private Logger logger;

    @Mock
    private JsonSchemaLoader jsonSchemaLoader;

    @Mock
    private PayloadExtractor payloadExtractor;

    @InjectMocks
    private FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator;

    @Test
    public void shouldLoadASchemaFromTheFileSystemByItsNameAndValidate() throws Exception {

        final String actionName = "example.action-name";
        final String envelopeJson = "{\"envelope\": \"json\"}";

        final JSONObject payload = mock(JSONObject.class);
        final Schema schema = mock(Schema.class);

        when(payloadExtractor.extractPayloadFrom(envelopeJson)).thenReturn(payload);
        when(jsonSchemaLoader.loadSchema(actionName)).thenReturn(schema);

        fileBasedJsonSchemaValidator.validateWithoutSchemaCatalog(envelopeJson, actionName);

        verify(schema).validate(payload);
        verify(logger).info("Falling back to file based schema lookup, no catalog schema found for: {}", actionName);
    }
}
