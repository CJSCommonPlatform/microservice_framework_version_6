package uk.gov.justice.services.adapters.rest.util.builder;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;

public class RamlResourceMethodBuilder {
    private static String METHOD_TEMPLATE = "  {0}:\r\n" +
            "    body:\r\n" +
            "{1}";
    private HttpMethod method;
    private String[] mediaType = {"application/vnd.default+json"};

    private RamlResourceMethodBuilder(HttpMethod method) {
        this.method = method;
    }

    public static RamlResourceMethodBuilder aResourceMethod(HttpMethod method) {
        return new RamlResourceMethodBuilder(method);
    }

    public RamlResourceMethodBuilder withConsumedMediaTypes(String... mediaType) {
        this.mediaType = mediaType;
        return this;

    }

    public String toString() {
        StringBuilder mediaTypeString = new StringBuilder();
        if (mediaType != null) {
            stream(mediaType).forEach(mt -> mediaTypeString.append("      ").append(mt).append(":\r\n"));
        }
        return format(METHOD_TEMPLATE, method.toString().toLowerCase(), mediaTypeString);

    }

    public static enum Method {
        POST, GET;
    }
}
