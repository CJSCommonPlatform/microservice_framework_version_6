package uk.gov.justice.services.messaging;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.messaging.JSONObjectValueObfuscator.obfuscated;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;


public class JSONObjectValueObfuscatorTest {

    @Test
    public void shouldReplaceStringValues() throws Exception {
        with(obfuscated(new JSONObject()
                .put("property1", "Hello World!")
                .put("nested", new JSONObject().put("property2", "Hello Universe!")))
                .toString())
                .assertThat("property1", is("xxx"))
                .assertThat("nested.property2", is("xxx"));

    }

    @Test
    public void shouldReplaceBooleanValues() throws Exception {
        with(obfuscated(new JSONObject()
                .put("property1", true)
                .put("property2", false)
                .put("nested", new JSONObject().put("property3", true)))
                .toString())
                .assertThat("property1", is(false))
                .assertThat("property2", is(false))
                .assertThat("nested.property3", is(false));

    }

    @Test
    public void shouldReplaceNumericValues() throws Exception {
        with(obfuscated(new JSONObject()
                .put("property1", 10)
                .put("property2", 13L)
                .put("nested", new JSONObject().put("property3", BigDecimal.valueOf(11111))))
                .toString())
                .assertThat("property1", is(0))
                .assertThat("property2", is(0))
                .assertThat("nested.property3", is(0));

    }

    @Test
    public void shouldReplaceUUIDs() throws Exception {
        with(obfuscated(new JSONObject()
                .put("property1", randomUUID())
                .put("nested", new JSONObject().put("property2", randomUUID())))
                .toString())
                .assertThat("property1", is("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"))
                .assertThat("nested.property2", is("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"));
    }

    @Test
    public void shouldReplaceValuesInArray() throws Exception {
        with(obfuscated(new JSONObject()
                .put("property1", new JSONArray().put("value1").put("value2").put("value2"))
                .put("nested", new JSONObject().put("property2", new JSONArray().put(1).put(BigDecimal.valueOf(3333)).put(77777L))))
                .toString())
                .assertThat("property1", hasItems("xxx", "xxx", "xxx"))
                .assertThat("nested.property2", hasItems(0, 0, 0));
    }

    @Test
    public void shouldObfuscateJsonObjectInArray() throws Exception {
        final JSONObject json = new JSONObject()
                .put("property1", new JSONArray().put(new JSONObject().put("property2", "someValueA")).put(new JSONObject().put("property3", "someValueB")));
        final String obfuscatedJson = obfuscated(json)
                .toString();
        System.out.println(json);
        System.out.println(obfuscatedJson);

        with(obfuscatedJson)
                .assertThat("property1[0].property2", is("xxx"))
                .assertThat("property1[1].property3", is("xxx"));
    }

}