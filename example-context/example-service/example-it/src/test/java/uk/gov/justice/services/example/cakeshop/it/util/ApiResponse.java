package uk.gov.justice.services.example.cakeshop.it.util;

import javax.ws.rs.core.Response;


/**
 * REST API response
 */
public class ApiResponse {

    private final int httpCode;
    private final String body;

    private ApiResponse(int httpCode, String body) {
        this.httpCode = httpCode;
        this.body = body;
    }

    /**
     * @param jaxsRsResponse - JAX-RS response
     * @return API response containig body and httpCode
     */
    public static ApiResponse from(Response jaxsRsResponse) {
        return new ApiResponse(jaxsRsResponse.getStatus(), jaxsRsResponse.readEntity(String.class));
    }

    public int httpCode() {
        return httpCode;
    }

    public String body() {
        return body;
    }
}
