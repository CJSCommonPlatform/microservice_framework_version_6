package uk.gov.justice.services.core.json;

import static org.apache.log4j.Level.TRACE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.test.utils.common.logger.TestLogAppender;

import org.apache.log4j.spi.LoggingEvent;
import org.everit.json.schema.Schema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
