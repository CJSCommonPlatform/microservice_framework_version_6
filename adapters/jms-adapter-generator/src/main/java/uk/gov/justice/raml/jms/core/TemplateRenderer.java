package uk.gov.justice.raml.jms.core;

import static java.lang.String.format;

import java.util.Map;

import org.apache.commons.lang.text.StrBuilder;

/**
 * Basic template rendering class
 *
 */
public class TemplateRenderer {
    public static final String ATTRIBUTE_KEY_FORMAT = "${%s}";

    private TemplateRenderer() {
    }

    /**
     * @param template - string containing template
     * @param attributes - attribute keys and values to be used in rendering of the template 
     * @return - rendered template
     */
    public static String render(final String template, final Map<String, String> attributes) {
        final StrBuilder stringBuilder = new StrBuilder(template);
        attributes.entrySet().forEach(e -> stringBuilder.replaceAll(format(ATTRIBUTE_KEY_FORMAT, e.getKey()), e.getValue()));
        return stringBuilder.toString();
    }
}
