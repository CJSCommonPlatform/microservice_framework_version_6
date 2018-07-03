package uk.gov.justice.services.core.json;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.schema.catalog.SchemaCatalogResolver;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
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

    @Mock
    private SchemaCatalogResolver schemaCatalogResolver;

    @InjectMocks
    private JsonSchemaLoader loader;

    @Test
    public void shouldReturnSchemaFromClasspath() {
        final Schema expectedSchema = mock(Schema.class);
        when(schemaCatalogResolver.loadSchema(any(JSONObject.class))).thenReturn(expectedSchema);
        final Schema actualSchema = loader.loadSchema("test-schema");

        assertThat(actualSchema, is(expectedSchema));
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
        expectedException.expectMessage("Unable to load JSON schema /json/schema/non-existent.json from classpath");
        loader.loadSchema("non-existent");
    }

    @Test
    public void shouldThrowExceptionIfSchemaMalformed() {
        expectedException.expect(SchemaLoadingException.class);
        expectedException.expectMessage("Unable to load JSON schema /json/schema/malformed-schema.json from classpath");

        loader.loadSchema("malformed-schema");
    }
}
