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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * Unit tests for the {@link JsonSchemaValidator} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaValidatorTest {

    @Mock
    private JsonSchemaLoader loader;

    @InjectMocks
    private JsonSchemaValidator validator;

    @Test
    public void shouldValidateUsingCorrectSchema() {

        final String json = "{\"rhubarb\": \"value\"}";

        Schema schema = mock(Schema.class);
        when(loader.loadSchema("test-schema")).thenReturn(schema);

        validator.validate(json, "test-schema");

        verify(schema).validate(argThat(equalToJSONObject(new JSONObject(json))));
    }

    @Test
    public void shouldRemoveMetadataFieldFromJsonToBeValidated() {

        final String json = "{\"rhubarb\": \"value\"}";
        final String jsonWithMetadata = "{\"_metadata\": {}, \"rhubarb\": \"value\"}";

        Schema schema = mock(Schema.class);
        when(loader.loadSchema("test-schema")).thenReturn(schema);

        validator.validate(jsonWithMetadata, "test-schema");

        verify(schema).validate(argThat(equalToJSONObject(new JSONObject(json))));
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
