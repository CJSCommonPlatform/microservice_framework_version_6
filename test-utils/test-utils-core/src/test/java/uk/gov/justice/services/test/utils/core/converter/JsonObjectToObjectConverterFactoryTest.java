package uk.gov.justice.services.test.utils.core.converter;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class JsonObjectToObjectConverterFactoryTest {

    @Test
    public void testUsableJsonObjectToObjectConverterIsProduced(){
        JsonObjectToObjectConverter converter = JsonObjectToObjectConverterFactory.createJsonObjectToObjectConverter();

        assertThat(converter, is(notNullValue()));

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("testAttribute", "testValue")
                .build();

        TestPojo convertedPojo = converter.convert(jsonObject, TestPojo.class);
        assertThat(convertedPojo.getTestAttribute(), is("testValue"));
    }

    private static class TestPojo {

        private final String testAttribute;

        private TestPojo(final String testAttribute) {
            this.testAttribute = testAttribute;
        }

        public String getTestAttribute() {
            return testAttribute;
        }
    }
}
