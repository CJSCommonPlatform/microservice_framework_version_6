package uk.gov.justice.services.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.exception.ConverterException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static javax.json.JsonValue.NULL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ObjectToJsonValueConverterTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String NAME = "Pojo";
    private static final List<String> ATTRIBUTES = Arrays.asList("Attribute 1", "Attribute 2");
    private static final String ATTRIBUTE_1 = "Attribute 1";
    private static final String ATTRIBUTE_2 = "Attribute 2";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ObjectMapper mapper;

    @Test
    public void shouldConvertPojoToJsonValue() throws Exception {
        Pojo pojo = new Pojo(ID, NAME, ATTRIBUTES);
        ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter();
        objectToJsonValueConverter.mapper = new JacksonMapperProducer().objectMapper();

        JsonValue jsonValue = objectToJsonValueConverter.convert(pojo);

        assertThat(jsonValue, equalTo(expectedJsonValue()));
    }

    @Test
    public void shouldConvertNullToJsonValueNull() throws Exception {
        ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter();

        JsonValue jsonValue = objectToJsonValueConverter.convert(null);

        assertThat(jsonValue, equalTo(NULL));
    }

    @Test
    public void shouldConvertListToJsonValue() throws Exception {
        List<String> list = Arrays.asList(ATTRIBUTE_1, ATTRIBUTE_2);
        ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter();
        objectToJsonValueConverter.mapper = new JacksonMapperProducer().objectMapper();

        JsonValue jsonValue = objectToJsonValueConverter.convert(list);

        assertThat(jsonValue, equalTo(expectedJsonArray()));
    }

    @Test
    public void shouldThrowExceptionOnConversionError() throws JsonProcessingException {
        ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter();
        objectToJsonValueConverter.mapper = mapper;

        Pojo pojo = new Pojo(ID, NAME, ATTRIBUTES);
        doThrow(JsonProcessingException.class).when(mapper).writeValueAsString(pojo);

        exception.expect(IllegalArgumentException.class);
        exception.expectCause(isA(JsonProcessingException.class));

        objectToJsonValueConverter.convert(pojo);
    }

    @Test(expected = ConverterException.class)
    public void shouldThrowExceptionOnNullResult() throws JsonProcessingException {
        ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter();
        objectToJsonValueConverter.mapper = mapper;

        Pojo pojo = new Pojo(ID, NAME, ATTRIBUTES);
        when(mapper.writeValueAsString(pojo)).thenReturn(null);

        objectToJsonValueConverter.convert(pojo);
    }

    private JsonValue expectedJsonValue() {
        JsonArray array = Json.createArrayBuilder()
                .add("Attribute 1")
                .add("Attribute 2").build();

        return Json.createObjectBuilder()
                .add("id", ID.toString())
                .add("name", NAME)
                .add("attributes", array).build();
    }

    private JsonValue expectedJsonArray() {
        return Json.createArrayBuilder()
                .add(ATTRIBUTE_1)
                .add(ATTRIBUTE_2).build();
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
