package uk.gov.justice.services.messaging;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.exception.InvalidMediaTypeException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Extracts name from media type. <p>Expected media type format: "application/vnd.[NAME]+json"
     *
     * @param mediaType media type to extract the name from.
     * @return extracted media type name.
     * @throws InvalidMediaTypeException If name not found inside media type.
     */
    public static Name fromMediaType(final String mediaType) {
        try {
            final Matcher matcher = MEDIA_TYPE_PATTERN.matcher(mediaType);
            matcher.find();
            return new Name(matcher.group(2));
        } catch (IllegalStateException e) {
            throw new InvalidMediaTypeException(format("Error extracting name from media type %s.", mediaType), e);
        }
    }

}
