package uk.gov.justice.services.generators.test.utils.builder;


import javax.ws.rs.core.HttpHeaders;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;

public class HeadersBuilder {
    public static HttpHeaders headersWith(final String headerName, final String headerValue) {
        final MultivaluedMapImpl headersMap = new MultivaluedMapImpl();
        headersMap.add(headerName, headerValue);
        return new ResteasyHttpHeaders(headersMap);
    }

}
