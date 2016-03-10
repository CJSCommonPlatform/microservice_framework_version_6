package uk.gov.justice.raml.jms.core;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TemplateMarkerTest {
    
    @Test
    public void shouldReplaceMarkedPosition() throws Exception {
        final String template = "Test ${replace}";
        final Map<String, String> data = new HashMap<>();
        data.put("replace", "replacement");

        final String result = TemplateMarker.render(template, data);
        assertThat(result, equalTo("Test replacement"));
    }
    
    @Test
    public void shouldReplaceMultipleMarkedPositionsOfSameKey() throws Exception {
        final String template = "${replace} Test ${replace}";
        final Map<String, String> data = new HashMap<>();
        data.put("replace", "replacement");

        final String result = TemplateMarker.render(template, data);
        assertThat(result, equalTo("replacement Test replacement"));
    }

    @Test
    public void shouldReplaceMultipleMarkedPositionsOfDifferentKey() throws Exception {
        final String template = "${replace} Test ${different} ${replace} Test ${different}";
        final Map<String, String> data = new HashMap<>();
        data.put("replace", "replacement");
        data.put("different", "other");

        final String result = TemplateMarker.render(template, data);
        assertThat(result, equalTo("replacement Test other replacement Test other"));
    }

}
