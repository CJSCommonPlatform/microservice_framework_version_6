package uk.gov.justice.subscription.yaml.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class YamlToJsonObjectConverterTest {

    @Mock
    private YamlParser yamlParser;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private YamlToJsonObjectConverter yamlToJsonObjectConverter;

    @Test
    public void shouldConvertYamlToJsonObject() throws Exception {
        final URL yamlUrl = new URL("file:/test");
        final Object yamlObject = mock(Object.class);

        when(yamlParser.parseYamlFrom(yamlUrl, Object.class)).thenReturn(yamlObject);
        when(objectMapper.writeValueAsString(yamlObject)).thenReturn("{\"test\": \"value\"}");

        final JSONObject jsonObject = yamlToJsonObjectConverter.convert(yamlUrl);

        assertThat(jsonObject.get("test"), is("value"));
    }

    @Test
    public void shouldThrowExceptionIfFailsToConvertYamlToJsonString() throws Exception {
        final URL yamlUrl = new URL("file:/test");
        final Object yamlObject = mock(Object.class);

        when(yamlParser.parseYamlFrom(yamlUrl, Object.class)).thenReturn(yamlObject);
        when(objectMapper.writeValueAsString(yamlObject)).thenThrow(mock(JsonProcessingException.class));

        try {
            yamlToJsonObjectConverter.convert(yamlUrl);
            fail("Expected exception to be thrown");
        } catch (final YamlToJsonObjectException e) {
            assertThat(e.getMessage(), containsString("Failed to convert YAML to JSON for"));
            assertThat(e.getMessage(), containsString("file:/test"));
            assertThat(e.getCause(), is(instanceOf(JsonProcessingException.class)));
        }
    }
}
