package uk.gov.justice.services.generators.test.utils.builder;

import static java.util.Arrays.stream;

import java.util.HashMap;
import java.util.Map;

import org.raml.model.MimeType;
import org.raml.model.Response;

public class ResponseBuilder {

    private final Map<String, MimeType> responseBody = new HashMap<>();
    private String description = "";

    public static ResponseBuilder response() {
        return new ResponseBuilder();
    }

    public ResponseBuilder withDescriptionOf(final String description) {
        this.description = description;
        return this;
    }

    public ResponseBuilder withBodyTypes(final String... responseTypes) {
        stream(responseTypes).forEach(type ->
                this.responseBody.put(type, new MimeType(type))
        );
        return this;
    }

    public Response build() {
        final Response response = new Response();
        response.setDescription(description);
        response.setBody(responseBody);
        return response;
    }
}
