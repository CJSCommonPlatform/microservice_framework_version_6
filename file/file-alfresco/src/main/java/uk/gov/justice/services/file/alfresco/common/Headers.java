package uk.gov.justice.services.file.alfresco.common;


import static java.util.Collections.singletonList;

import javax.ws.rs.core.MultivaluedHashMap;

public class Headers {

    public static final String ALFRESCO_USER_ID = "cppuid";

    public static MultivaluedHashMap<String, Object> headersWithUserId(final String id) {
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put(ALFRESCO_USER_ID, singletonList(id));
        return headers;
    }
}
