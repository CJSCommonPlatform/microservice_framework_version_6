package uk.gov.justice.raml.jms.core;

import static java.lang.String.format;

import java.util.Map;

import org.apache.commons.lang.text.StrBuilder;

public class TemplateMarker {
    public static final String MARKER_FORMAT = "${%s}";

    private TemplateMarker() {
    }

    public static String mark(final String template, final Map<String, String> data) {
        final StrBuilder stringBuilder = new StrBuilder(template);
        data.entrySet().forEach(e -> stringBuilder.replaceAll(format(MARKER_FORMAT, e.getKey()), e.getValue()));
        return stringBuilder.toString();
    }
}
