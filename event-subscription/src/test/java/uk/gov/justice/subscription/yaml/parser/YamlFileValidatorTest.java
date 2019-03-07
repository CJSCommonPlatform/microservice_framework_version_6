package uk.gov.justice.subscription.yaml.parser;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class YamlFileValidatorTest {

    private static final String EVENT_SOURCES_SCHEMA_PATH = "/json/schema/event-source-schema.json";
    private static final String SUBSCRIPTION_SCHEMA_PATH = "/json/schema/subscription-schema.json";

    @Mock
    private YamlSchemaLoader yamlSchemaLoader;

    @Mock
    private YamlToJsonObjectConverter yamlToJsonObjectConverter;

    @InjectMocks
    private YamlFileValidator yamlFileValidator;

    @Test
    public void shouldValidateEventSourceYamlFile() throws IOException {
        final URL yamlUrl = new URL("file:/test");
        final JSONObject jsonObject = mock(JSONObject.class);
        final Schema schema = mock(Schema.class);

        when(yamlToJsonObjectConverter.convert(yamlUrl)).thenReturn(jsonObject);
        when(yamlSchemaLoader.loadSchema(EVENT_SOURCES_SCHEMA_PATH)).thenReturn(schema);

        yamlFileValidator.validateEventSource(yamlUrl);

        verify(schema).validate(jsonObject);
    }

    @Test
    public void shouldValidateSubscriptionYamlFile() throws IOException {
        final URL yamlUrl = new URL("file:/test");
        final JSONObject jsonObject = mock(JSONObject.class);
        final Schema schema = mock(Schema.class);

        when(yamlToJsonObjectConverter.convert(yamlUrl)).thenReturn(jsonObject);
        when(yamlSchemaLoader.loadSchema(SUBSCRIPTION_SCHEMA_PATH)).thenReturn(schema);

        yamlFileValidator.validateSubscription(yamlUrl);

        verify(schema).validate(jsonObject);
    }

    @Test
    public void shouldThrowYamlParserException() throws IOException {
        try {
            final URL yamlUrl = new URL("file:/test");
            final JSONObject jsonObject = mock(JSONObject.class);

            when(yamlToJsonObjectConverter.convert(yamlUrl)).thenReturn(jsonObject);
            when(yamlSchemaLoader.loadSchema(SUBSCRIPTION_SCHEMA_PATH)).thenThrow(new IOException());

            yamlFileValidator.validateSubscription(yamlUrl);
            fail();
        } catch (final Exception expected) {
            assertThat(expected, is(instanceOf(YamlParserException.class)));
            assertThat(expected.getMessage(), containsString("Unable to load JSON schema"));
        }
    }
}
