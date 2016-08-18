package uk.gov.justice.services.common.converter;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.exception.ConverterException;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObjectToJsonObjectConverterTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String NAME = "Pojo";
    private static final List<String> ATTRIBUTES = Arrays.asList("Attribute 1", "Attribute 2");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ObjectMapper mapper;

    @Test
    public void shouldConvertPojoToJsonObject() throws Exception {
        Pojo pojo = new Pojo(ID, NAME, ATTRIBUTES);
        ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
        objectToJsonObjectConverter.mapper = new ObjectMapperProducer().objectMapper();

        JsonObject jsonObject = objectToJsonObjectConverter.convert(pojo);

        assertThat(jsonObject, equalTo(expectedJsonObject()));
    }

    @Test
    public void shouldThrowExceptionOnConversionError() throws JsonProcessingException {
        ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
        objectToJsonObjectConverter.mapper = mapper;

        Pojo pojo = new Pojo(ID, NAME, ATTRIBUTES);
        doThrow(JsonProcessingException.class).when(mapper).writeValueAsString(pojo);

        exception.expect(IllegalArgumentException.class);
        exception.expectCause(isA(JsonProcessingException.class));

        objectToJsonObjectConverter.convert(pojo);
    }

    @Test(expected = ConverterException.class)
    public void shouldThrowExceptionOnNullResult() throws JsonProcessingException {
        ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
        objectToJsonObjectConverter.mapper = mapper;

        Pojo pojo = new Pojo(ID, NAME, ATTRIBUTES);
        when(mapper.writeValueAsString(pojo)).thenReturn(null);

        objectToJsonObjectConverter.convert(pojo);
    }

    private JsonObject expectedJsonObject() {
        JsonArray array = createArrayBuilder()
                .add("Attribute 1")
                .add("Attribute 2").build();

        return createObjectBuilder()
                .add("id", ID.toString())
                .add("name", NAME)
                .add("attributes", array).build();
    }

    public static class Pojo {

        private final UUID id;
        private final String name;
        private final List<String> attributes;

        public Pojo(UUID id, String name, List<String> attributes) {
            this.id = id;
            this.name = name;
            this.attributes = attributes;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<String> getAttributes() {
            return attributes;
        }
    }
}
