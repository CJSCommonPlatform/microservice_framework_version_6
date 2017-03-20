package uk.gov.justice.services.test.utils.core.matchers;

import static javax.json.Json.createObjectBuilder;
import static org.junit.Assert.assertThat;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Test;

public class JsonValueNullMatcherTest {

    @Test
    public void shouldMatchJsonValueNull() throws Exception {
        assertThat(JsonValue.NULL, JsonValueNullMatcher.isJsonValueNull());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonObject() throws Exception {
        final JsonObject jsonObject = createObjectBuilder()
                .add("someId", "idValue")
                .build();

        assertThat(jsonObject, JsonValueNullMatcher.isJsonValueNull());
    }
}