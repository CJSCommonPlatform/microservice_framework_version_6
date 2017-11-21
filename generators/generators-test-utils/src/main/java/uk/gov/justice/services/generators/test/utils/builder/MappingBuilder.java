package uk.gov.justice.services.generators.test.utils.builder;

import static java.text.MessageFormat.format;

/**
 * Builds RAML snippet defining mapping between action and media type
 */
public class MappingBuilder {

    private static final String REQUEST_TYPE = "requestType";
    private static final String RESPONSE_TYPE = "responseType";
    private static final String MAPPING_TEMPLATE =
            "(mapping):\n" +
                    "    {0}: {1}\n" +
                    "    name: {2}\n";

    private static final String REQUEST_AND_RESPONSE_MAPPING_TEMPLATE =
            "(mapping):\n" +
                    "    {0}: {1}\n" +
                    "    {2}: {3}\n" +
                    "    name: {4}\n";

    private String requestType;
    private String responseType;
    private String name;

    public static MappingBuilder mapping() {
        return new MappingBuilder();
    }

    public static MappingBuilder defaultMapping() {
        return new MappingBuilder()
                .withRequestType("application/vnd.blah+json")
                .withName("name1");
    }

    public MappingBuilder withRequestType(final String requestType) {
        this.requestType = requestType;
        return this;
    }

    public MappingBuilder withResponseType(final String responseType) {
        this.responseType = responseType;
        return this;
    }


    public MappingBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public String build() {
        if (null != requestType && null != responseType) {
            return format(REQUEST_AND_RESPONSE_MAPPING_TEMPLATE,
                    REQUEST_TYPE,
                    requestType,
                    RESPONSE_TYPE,
                    responseType,
                    name);
        }

        return format(MAPPING_TEMPLATE,
                requestType != null ? REQUEST_TYPE : RESPONSE_TYPE,
                requestType != null ? requestType : responseType,
                name);
    }
}
