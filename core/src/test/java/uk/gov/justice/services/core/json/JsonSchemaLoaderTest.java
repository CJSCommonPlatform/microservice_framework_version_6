package uk.gov.justice.services.core.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;

import org.everit.json.schema.Schema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfSchemaCouldNotBeLoaded() {
        loader.loadSchema("non-existent");
    }
}
