package uk.gov.justice.services.adapter.rest.mapping;

import javax.ws.rs.core.HttpHeaders;

public interface ActionMapper {

    String actionOf(final String methodName, final String httpMethod, final HttpHeaders headers);
}