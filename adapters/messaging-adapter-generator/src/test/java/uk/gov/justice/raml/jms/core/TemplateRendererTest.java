package uk.gov.justice.raml.jms.core;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;


public class TemplateRendererTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(TemplateRenderer.class);
    }

    @Test
    public void shouldReplaceMarkedPosition() throws Exception {
        String template = "Test ${replace}";
        Map<String, String> data = new HashMap<>();
        data.put("replace", "replacement");

        String result = TemplateRenderer.render(template, data);
        assertThat(result, equalTo("Test replacement"));
    }

    @Test
    public void shouldNotReplacePosition() throws Exception {
        String template = "Test replace";
        Map<String, String> data = new HashMap<>();
        data.put("replace", "replacement");

        String result = TemplateRenderer.render(template, data);
        assertThat(result, equalTo("Test replace"));
    }

    @Test
    public void shouldReplaceMultipleMarkedPositionsOfSameKey() throws Exception {
        String template = "${replace} Test ${replace}";
        Map<String, String> data = new HashMap<>();
        data.put("replace", "replacement");

        String result = TemplateRenderer.render(template, data);
        assertThat(result, equalTo("replacement Test replacement"));
    }

    @Test
    public void shouldReplaceMultipleMarkedPositionsOfDifferentKey() throws Exception {
        String template = "${replace} Test ${different} ${replace} Test ${different}";
        Map<String, String> data = new HashMap<>();
        data.put("replace", "replacement");
        data.put("different", "other");

        String result = TemplateRenderer.render(template, data);
        assertThat(result, equalTo("replacement Test other replacement Test other"));
    }

}
