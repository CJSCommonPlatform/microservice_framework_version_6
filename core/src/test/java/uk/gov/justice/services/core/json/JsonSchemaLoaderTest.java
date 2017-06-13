package uk.gov.justice.services.core.json;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.everit.json.schema.Schema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link JsonSchemaLoader} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaLoaderTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private JsonSchemaLoader loader;

    @Test
    public void shouldReturnSchemaFromClasspath() {
        Schema schema = loader.loadSchema("test-schema");
        assertThat(schema.getId(), equalTo("test-schema"));
    }

    @Test
    public void shouldLogSchemaName() throws Exception {
        loader.loadSchema("test-schema");
        verify(logger).trace("Loading schema {}", "/json/schema/test-schema.json");
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void shouldThrowExceptionIfSchemaNotFound() {
        expectedException.expect(SchemaLoadingException.class);
        expectedException.expectMessage("JSON schema /json/schema/non-existent.json not found on classpath");
        loader.loadSchema("non-existent");
    }

    @Test
    public void shouldThrowExceptionIfSchemaMalformed() {
        expectedException.expect(SchemaLoadingException.class);
        expectedException.expectMessage("Unable to load JSON schema /json/schema/malformed-schema.json from classpath");

        loader.loadSchema("malformed-schema");
    }
}
