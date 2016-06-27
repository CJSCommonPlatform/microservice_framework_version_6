package uk.gov.justice.services.example.cakeshop.it.util;

import javax.ws.rs.core.Response;


/**
 * REST API response
 */
public class ApiResponse {

    private final int httpCode;
    private final String body;

    private ApiResponse(final int httpCode, final String body) {
        this.httpCode = httpCode;
        this.body = body;
    }

    /**
     * @param jaxsRsResponse - JAX-RS response
     * @return API response containig body and httpCode
     */
    public static ApiResponse from(final Response jaxsRsResponse) {
        final String responseBody = jaxsRsResponse.readEntity(String.class);
        jaxsRsResponse.close();
        return new ApiResponse(jaxsRsResponse.getStatus(), responseBody);
    }

    public int httpCode() {
        return httpCode;
    }

    public String body() {
        return body;
    }
}
