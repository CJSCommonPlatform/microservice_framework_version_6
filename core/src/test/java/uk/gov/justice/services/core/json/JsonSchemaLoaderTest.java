package uk.gov.justice.services.core.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import uk.gov.justice.services.core.json.JsonSchemaLoader;

import org.everit.json.schema.Schema;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link JsonSchemaLoader} class.
 */
public class JsonSchemaLoaderTest {

    private JsonSchemaLoader loader;

    @Before
    public void setup() {
        loader = new JsonSchemaLoader();
    }

    @Test
    public void shouldReturnSchemaFromClasspath() {
        Schema schema = loader.loadSchema("test-schema");
        assertThat(schema.getId(), equalTo("test-schema"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfSchemaCouldNotBeLoaded() {
        loader.loadSchema("non-existent");
    }
}
