package uk.gov.justice.raml.jms.core;

import java.util.Map;

public class TemplateMarker {
    public static final String MARKER_FORMAT = "\\$\\{%s\\}";

    private TemplateMarker() {
    }

    public static String mark(final String template, final Map<String, String> data) {
        String temp = template;

        for (Map.Entry<String, String> entries : data.entrySet()) {
            temp = temp.replaceAll(String.format(MARKER_FORMAT, entries.getKey()), entries.getValue());
        }

        return temp;
    }
}
