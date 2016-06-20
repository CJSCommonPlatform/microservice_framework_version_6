package uk.gov.justice.services.common.converter;

import static com.google.common.io.Resources.getResource;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

public class ObjectMapperProducerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapperProducer().objectMapper();
    }

    @Test
    public void shouldReturnAMapper() throws Exception {
        assertThat(mapper, notNullValue());
        assertThat(mapper, isA(ObjectMapper.class));
    }

    @Test
    public void shouldOmitsNullValuesFromObjects() throws Exception {
        final Object source = new Object() {
            public String getId() {
                return "861c9430-7bc6-4bf0-b549-6534394b8d65";
            }

            public String getName() {
                return null;
            }
        };

        final String json = mapper.writeValueAsString(source);

        assertEquals(jsonFromFile("test-with-one-field"), json, true);
    }

    @Test
    public void shouldOmitsNullValuesFromJsonObjects() throws Exception {
        final JsonObject source = createObjectBuilder()
                .add("id", "861c9430-7bc6-4bf0-b549-6534394b8d65")
                .add("name", JsonValue.NULL)
                .build();

        final String json = mapper.writeValueAsString(source);

        assertEquals(jsonFromFile("test-with-one-field"), json, true);
    }

    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(getResource(format("json/%s.json", name)), defaultCharset());
    }
}
