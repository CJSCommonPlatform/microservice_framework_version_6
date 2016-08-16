package uk.gov.justice.services.core.json;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

import org.everit.json.schema.Schema;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link JsonSchemaValidator} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaValidatorTest {

    private static final String TEST_SCHEMA_NAME = "test-schema";

    @Mock
    private Logger logger;

    @Mock
    private JsonSchemaLoader loader;

    @Mock
    private Schema schema;

    @InjectMocks
    private JsonSchemaValidator validator;

    @Before
    public void setuo() {
        when(loader.loadSchema(TEST_SCHEMA_NAME)).thenReturn(schema);
    }

    @Test
    public void shouldValidateUsingCorrectSchema() {
        final String json = "{\"rhubarb\": \"value\"}";

        validator.validate(json, TEST_SCHEMA_NAME);

        verify(schema).validate(argThat(equalToJSONObject(new JSONObject(json))));
        assertLogStatement();
    }

    @Test
    public void shouldRemoveMetadataFieldFromJsonToBeValidated() {
        final String json = "{\"rhubarb\": \"value\"}";
        final String jsonWithMetadata = "{\"_metadata\": {}, \"rhubarb\": \"value\"}";

        validator.validate(jsonWithMetadata, TEST_SCHEMA_NAME);

        verify(schema).validate(argThat(equalToJSONObject(new JSONObject(json))));
        assertLogStatement();
    }

    private void assertLogStatement() {
        verify(logger).trace("Performing schema validation for: {}", TEST_SCHEMA_NAME);
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
