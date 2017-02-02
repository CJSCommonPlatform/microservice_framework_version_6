package uk.gov.justice.services.core.json;

import static org.apache.log4j.Level.TRACE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.test.utils.common.logger.TestLogAppender;

import org.apache.log4j.spi.LoggingEvent;
import org.everit.json.schema.Schema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link JsonSchemaLoader} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaLoaderTest {

    private JsonSchemaLoader loader = new JsonSchemaLoader();

    @Test
    public void shouldReturnSchemaFromClasspath() {
        Schema schema = loader.loadSchema("test-schema");
        assertThat(schema.getId(), equalTo("test-schema"));
    }

    @Test
    public void shouldLogSchemaName() throws Exception {
        final TestLogAppender testLogAppender = TestLogAppender.activate();

        loader.loadSchema("test-schema");
        testLogAppender.deactivate();
        final LoggingEvent logEntry = testLogAppender.firstLogEntry();
        assertThat(logEntry.getLevel(), is(TRACE));
        assertThat(logEntry.getMessage(), is("Loading schema /json/schema/test-schema.json"));

    }

    @Test(expected = SchemaLoadingException.class)
    public void shouldThrowExceptionIfSchemaCouldNotBeLoaded() {
        loader.loadSchema("non-existent");
    }
}
