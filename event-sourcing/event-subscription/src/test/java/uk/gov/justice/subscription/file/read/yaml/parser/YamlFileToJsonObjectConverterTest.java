package uk.gov.justice.subscription.file.read.yaml.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.yaml.parser.YamlFileToJsonObjectConverter;
import uk.gov.justice.subscription.yaml.parser.YamlParser;
import uk.gov.justice.subscription.yaml.parser.YamlToJsonObjectException;

import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class YamlFileToJsonObjectConverterTest {

    @Mock
    private YamlParser yamlParser;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private YamlFileToJsonObjectConverter yamlFileToJsonObjectConverter;

    @Test
    public void shouldConvertYamlToJsonObject() throws Exception {
        final Path yamlPath = mock(Path.class);
        final Object yamlObject = mock(Object.class);

        when(yamlParser.parseYamlFrom(yamlPath, Object.class)).thenReturn(yamlObject);
        when(objectMapper.writeValueAsString(yamlObject)).thenReturn("{\"test\": \"value\"}");

        final JSONObject jsonObject = yamlFileToJsonObjectConverter.convert(yamlPath);

        assertThat(jsonObject.get("test"), is("value"));
    }

    @Test
    public void shouldThrowExceptionIfFailsToConvertYamlToJsonString() throws Exception {
        final Path yamlPath = mock(Path.class);
        final Path absolutePath = mock(Path.class);
        final Object yamlObject = mock(Object.class);

        when(yamlParser.parseYamlFrom(yamlPath, Object.class)).thenReturn(yamlObject);
        when(yamlPath.toAbsolutePath()).thenReturn(absolutePath);
        when(absolutePath.toString()).thenReturn("test/path");
        when(objectMapper.writeValueAsString(yamlObject)).thenThrow(mock(JsonProcessingException.class));

        try {
            yamlFileToJsonObjectConverter.convert(yamlPath);
            fail("Expected exception to be thrown");
        } catch (final YamlToJsonObjectException e) {
            assertThat(e.getMessage(), containsString("Failed to convert YAML to JSON for"));
            assertThat(e.getMessage(), containsString("test/path"));
            assertThat(e.getCause(), is(instanceOf(JsonProcessingException.class)));
        }
    }
}
