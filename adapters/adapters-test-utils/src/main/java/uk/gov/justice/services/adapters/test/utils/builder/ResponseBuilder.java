package uk.gov.justice.services.adapters.test.utils.builder;

import org.raml.model.Response;

public class ResponseBuilder {
    public static Response response(final String description) {
        final Response response = new Response();
        response.setDescription(description);
        return response;
    }
}
