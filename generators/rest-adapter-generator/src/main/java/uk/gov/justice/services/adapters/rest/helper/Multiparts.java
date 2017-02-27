package uk.gov.justice.services.adapters.rest.helper;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

import org.raml.model.MimeType;

final public class Multiparts {

    private Multiparts() {
    }

    public static boolean isMultipartResource(final MimeType bodyMimeType) {
        final String type = bodyMimeType.getType();
        return APPLICATION_FORM_URLENCODED.equals(type) || MULTIPART_FORM_DATA.equals(type);
    }
}