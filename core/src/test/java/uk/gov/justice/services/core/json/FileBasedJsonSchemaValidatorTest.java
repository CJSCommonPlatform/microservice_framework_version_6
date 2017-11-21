package uk.gov.justice.services.core.json;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;

import org.everit.json.schema.Schema;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link FileBasedJsonSchemaValidator} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileBasedJsonSchemaValidatorTest {

    private static final String TEST_SCHEMA_NAME = "test-schema";

    @Mock
    private Schema schema;

    @Mock
    private Logger logger;

    @Mock
    private JsonSchemaLoader loader;

    @Mock
    private PayloadExtractor payloadExtractor;

    @Mock
    private NameToMediaTypeConverter nameToMediaTypeConverter;

    @InjectMocks
    private FileBasedJsonSchemaValidator validator;

    @Test
    public void shouldValidateUsingCorrectSchema() {
        final String json = "{\"rhubarb\": \"value\"}";
        final MediaType mediaType = mock(MediaType.class);

        when(nameToMediaTypeConverter.convert(mediaType)).thenReturn(TEST_SCHEMA_NAME);
        when(loader.loadSchema(TEST_SCHEMA_NAME)).thenReturn(schema);
        when(payloadExtractor.extractPayloadFrom(json)).thenReturn(new JSONObject(json));

        validator.validate(json, mediaType);

        verify(schema).validate(argThat(equalToJSONObject(new JSONObject(json))));
        verify(logger).trace("Performing schema validation for: {}", "test-schema");
    }

    @Test
    public void shouldAcceptDateTimeWithSingleDigitInSecondsFraction() {
        final String json = "{\"testField\": \"value\", \"created\": \"2011-12-03T10:15:30.1Z\"}";
        final MediaType mediaType = mock(MediaType.class);

        when(nameToMediaTypeConverter.convert(mediaType)).thenReturn(TEST_SCHEMA_NAME);
        when(loader.loadSchema(TEST_SCHEMA_NAME)).thenReturn(schema);
        when(payloadExtractor.extractPayloadFrom(json)).thenReturn(new JSONObject(json));

        validator.validate(json, mediaType);

        verify(logger).trace("Performing schema validation for: {}", "test-schema");
    }

    private Matcher<JSONObject> equalToJSONObject(final JSONObject jsonObject) {
        return new TypeSafeMatcher<JSONObject>() {
            @Override
            protected boolean matchesSafely(JSONObject item) {
                return compareJSON(jsonObject, item, JSONCompareMode.STRICT).passed();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(jsonObject.toString());
            }
        };
    }
}
