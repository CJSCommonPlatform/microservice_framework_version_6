package uk.gov.justice.services.messaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

/**
 * Representation of a logical name.
 */
public class Name {

    private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("(application/vnd.)(\\S+)(\\+\\S+)");

    private final String name;

    private Name(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Name fromMediaType(final MediaType mediaType) {
        final Matcher m = MEDIA_TYPE_PATTERN.matcher(mediaType.getType() + "/" + mediaType.getSubtype());
        m.find();
        return new Name(m.group(2));
    }
}
