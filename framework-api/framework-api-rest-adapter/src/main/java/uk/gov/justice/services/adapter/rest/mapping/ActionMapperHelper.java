package uk.gov.justice.services.adapter.rest.mapping;

import javax.ws.rs.core.HttpHeaders;

public interface ActionMapperHelper {

    void add(final String methodName, final String mediaType, final String actionName);

    String actionOf(final String methodName, final String httpMethod, final HttpHeaders headers);
}