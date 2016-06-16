package uk.gov.justice.services.common.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.exception.ConverterException;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonObjectToObjectConverterTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID INTERNAL_ID = UUID.randomUUID();
    private static final String NAME = "Pojo";
    private static final String ATTRIBUTE_1 = "Attribute 1";
    private static final String ATTRIBUTE_2 = "Attribute 2";
    private static final String INTERNAL_NAME = "internalName";

    @Mock
    private ObjectMapper mapper;

    @Test
    public void shouldConvertPojoToJsonObject() throws Exception {
        JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
        jsonObjectToObjectConverter.mapper = new ObjectMapperProducer().objectMapper();

        JsonObject jsonObject = jsonObject();
        Pojo pojo = jsonObjectToObjectConverter.convert(jsonObject, Pojo.class);

        assertThat(pojo, notNullValue());
        assertThat(pojo.getId(), equalTo(ID));
        assertThat(pojo.getName(), equalTo(NAME));
        assertThat(pojo.getAttributes(), hasItems(ATTRIBUTE_1, ATTRIBUTE_2));
        assertThat(pojo.getInternalPojo().getInternalId(), equalTo(INTERNAL_ID));
        assertThat(pojo.getInternalPojo().getInternalName(), equalTo(INTERNAL_NAME));
    }

    @Test
    public void shouldConvertToPojoWithUTCDateTime() throws Exception {
        JsonObjectToObjectConverter converter = new JsonObjectToObjectConverter();
        converter.mapper = new ObjectMapperProducer().objectMapper();

        assertThat(converter.convert(Json.createObjectBuilder().add("dateTime", "2016-07-25T13:09:01.0+00:00").build(), PojoWithDateTime.class).getDateTime(),
                equalTo(ZonedDateTime.of(2016, 7, 25, 13, 9, 1, 0, ZoneId.of("UTC"))));
        assertThat(converter.convert(Json.createObjectBuilder().add("dateTime", "2016-07-25T13:09:01.0Z").build(), PojoWithDateTime.class).getDateTime(),
                equalTo(ZonedDateTime.of(2016, 7, 25, 13, 9, 1, 0, ZoneId.of("UTC"))));
        assertThat(converter.convert(Json.createObjectBuilder().add("dateTime", "2016-07-25T13:09:01Z").build(), PojoWithDateTime.class).getDateTime(),
                equalTo(ZonedDateTime.of(2016, 7, 25, 13, 9, 1, 0, ZoneId.of("UTC"))));
        assertThat(converter.convert(Json.createObjectBuilder().add("dateTime", "2016-07-25T16:09:01.0+03:00").build(), PojoWithDateTime.class).getDateTime(),
                equalTo(ZonedDateTime.of(2016, 7, 25, 13, 9, 1, 0, ZoneId.of("UTC"))));

    }

    @Test(expected = ConverterException.class)
    public void shouldThrowExceptionOnConversionError() throws IOException {
        JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
        jsonObjectToObjectConverter.mapper = mapper;
        JsonObject jsonObject = jsonObject();

        when(mapper.writeValueAsString(jsonObject)).thenReturn(null);

        jsonObjectToObjectConverter.convert(jsonObject, Pojo.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullResult() throws IOException {
        JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
        jsonObjectToObjectConverter.mapper = mapper;
        JsonObject jsonObject = jsonObject();

        doThrow(IOException.class).when(mapper).writeValueAsString(jsonObject);

        jsonObjectToObjectConverter.convert(jsonObject, Pojo.class);
    }

    private JsonObject jsonObject() {
        JsonArray array = Json.createArrayBuilder()
                .add(ATTRIBUTE_1)
                .add(ATTRIBUTE_2).build();

        return Json.createObjectBuilder()
                .add("id", ID.toString())
                .add("name", NAME)
                .add("internalPojo", Json.createObjectBuilder()
                        .add("internalId", INTERNAL_ID.toString())
                        .add("internalName", INTERNAL_NAME).build())
                .add("attributes", array).build();
    }

    public static class Pojo {

        private final UUID id;
        private final String name;
        private final List<String> attributes;
        private final InternalPojo internalPojo;


        public Pojo(UUID id, String name, List<String> attributes, InternalPojo internalPojo) {
            this.id = id;
            this.name = name;
            this.attributes = attributes;
            this.internalPojo = internalPojo;
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

        public InternalPojo getInternalPojo() {
            return internalPojo;
        }
    }

    public static class InternalPojo {

        private UUID internalId;
        private String internalName;

        public InternalPojo(final UUID internalId, final String internalName) {
            this.internalId = internalId;
            this.internalName = internalName;
        }

        public UUID getInternalId() {
            return internalId;
        }

        public String getInternalName() {
            return internalName;
        }
    }

    public static class PojoWithDateTime {
        private final ZonedDateTime dateTime;

        public PojoWithDateTime(@JsonProperty("dateTime") final ZonedDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public ZonedDateTime getDateTime() {
            return dateTime;
        }
    }

}
